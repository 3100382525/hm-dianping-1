package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result getList() {
        List<String> typelist = stringRedisTemplate.opsForList().range("cache:shop_type:", 0, -1);
        if (!typelist.isEmpty()) {
            List<ShopType> typeList = new ArrayList<>();
            for (String typeJson:typelist) {
                ShopType shopType = JSONUtil.toBean(typeJson, ShopType.class);
                typeList.add(shopType);
            }
            return Result.ok(typeList);
        }

        List<ShopType> shopTypes = query().orderByAsc("sort").list();

        if (shopTypes.isEmpty()) {
            return Result.fail("暂无商铺类型");
        }

        List<String> list = new ArrayList<>();

        for (ShopType shopType:shopTypes) {
            String jsonStr = JSONUtil.toJsonStr(shopType);
            list.add(jsonStr);
        }
        stringRedisTemplate.opsForList().rightPushAll("cache:shop_type:",list);
        return Result.ok(shopTypes);
    }
}
