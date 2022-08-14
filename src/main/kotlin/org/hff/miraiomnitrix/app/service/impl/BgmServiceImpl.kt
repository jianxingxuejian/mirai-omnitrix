package org.hff.miraiomnitrix.app.service.impl;

import org.hff.miraiomnitrix.app.entity.Bgm;
import org.hff.miraiomnitrix.app.mapper.BgmMapper;
import org.hff.miraiomnitrix.app.service.BgmService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
open class BgmServiceImpl : ServiceImpl<BgmMapper, Bgm>(), BgmService {

}
