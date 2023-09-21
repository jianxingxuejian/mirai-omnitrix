package org.hff.miraiomnitrix.utils

import jakarta.annotation.PreDestroy
import org.hff.miraiomnitrix.db.service.DomainNameService
import org.openqa.selenium.Dimension
import org.openqa.selenium.OutputType
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.time.Duration


private val domainNameMap = hashMapOf<String, Int>()
private val domainNameService = SpringUtil.getBean(DomainNameService::class)

@Component
object SeleniumUtil : CommandLineRunner {

    private lateinit var driver: ChromeDriver

    init {
        val os = System.getProperty("os.name").lowercase()
        val driverPath = System.getProperty("user.dir") + "/lib/"

        val options: ChromeOptions
        val driverName: String
        if (os.contains("win")) {
            driverName = "chromedriver-win64/chromedriver.exe"
            options = ChromeOptions().apply {
                addArguments("headless")
            }
        } else {
            driverName = "chromedriver-linux64/chromedriver"
            options = ChromeOptions().apply {
                addArguments("headless")
                addArguments("disable-gpu")
                addArguments("no-sandbox")
            }
        }

        System.setProperty("webdriver.chrome.driver", driverPath + driverName)

        driver = ChromeDriver(options).apply {
            manage().timeouts().apply {
                scriptTimeout(Duration.ofMinutes(1))
                pageLoadTimeout(Duration.ofMinutes(1))
                implicitlyWait(Duration.ofMinutes(1))
            }
        }
    }

    override fun run(vararg args: String?) {
        domainNameService.list().forEach { (_, domainName, state) ->
            domainNameMap[domainName] = state
        }
    }

    private const val CHECK_API = "https://api.kit9.cn/api/tencent_security/api.php?url="

    suspend fun screenshot(domainName: String, url: String): ByteArray? {
        if (!url.startsWith("http://") && !url.startsWith("https://")) return null
        val stateCache = domainNameMap[domainName]
        return when (stateCache) {
            2 -> null
            0, 1 -> screenshot(url)
            null -> try {
                // 使用腾讯域名检测接口，判断该域名是否安全，并将结果存到数据库
                val json = HttpUtil.getString(CHECK_API + url)
                val state = JsonUtil.getObj(json, "data").getAsInt("state")
                domainNameService.add(domainName, state)
                domainNameMap[domainName] = state
                if (state == 2) null
                else screenshot(url)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            else -> null
        }


    }

    private fun screenshot(url: String): ByteArray? {
        // 打开指定url
        driver.manage().window().size = Dimension(800, 1280)
        driver.get(url)

        // 判断页面是否加载完成，最多等待10秒
        val script = "return (window.performance.timing.loadEventEnd - window.performance.timing.navigationStart) >= 0;"
        val webDriverWait = WebDriverWait(driver, Duration.ofSeconds(20))
        try {
            webDriverWait.until(ExpectedConditions.jsReturnsValue(script))
        } catch (_: TimeoutException) {
        }

        //获取页面宽高,并设置为浏览器宽高然后截图
        val width = driver.executeScript("return document.documentElement.scrollWidth") as Long
        val height = driver.executeScript("return document.documentElement.scrollHeight") as Long
        driver.manage().window().size = Dimension(width.toInt(), minOf(height, width * 3).toInt())
        val screenshotBytes = driver.getScreenshotAs(OutputType.BYTES)

        driver.get("about:blank")
        return screenshotBytes
    }

    @PreDestroy
    fun exit() = try {
        driver.quit()
    } catch (_: Exception) {
    }

}
