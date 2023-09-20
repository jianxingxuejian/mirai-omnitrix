package org.hff.miraiomnitrix.utils

import org.hff.miraiomnitrix.db.service.DomainNameService
import org.openqa.selenium.Dimension
import org.openqa.selenium.OutputType
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.springframework.boot.CommandLineRunner
import java.time.Duration

object SeleniumUtil : CommandLineRunner {

    private val driver: ChromeDriver

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

    private val domainNameMap = hashMapOf<String, Int>()
    private val domainNameService = SpringUtil.getBean(DomainNameService::class)

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
        driver[url]
        val script =
            "return (window.performance.timing.loadEventEnd - window.performance.timing.navigationStart) >= 0;"
        val webDriverWait = WebDriverWait(driver, Duration.ofSeconds(50))
        webDriverWait.until(ExpectedConditions.jsReturnsValue(script))
        Thread.sleep(5000)
        val width = driver.executeScript("return document.documentElement.scrollWidth") as Long
        val height = driver.executeScript("return document.documentElement.scrollHeight") as Long
        driver.manage().window().size = Dimension(width.toInt(), height.toInt())
        val screenshotBytes = driver.getScreenshotAs(OutputType.BYTES)
        driver.manage().window().size = Dimension(600, 800)
        return screenshotBytes
    }

}
