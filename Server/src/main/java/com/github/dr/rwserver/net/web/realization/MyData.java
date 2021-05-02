package com.github.dr.rwserver.net.web.realization;

import com.github.dr.rwserver.net.web.realization.def.ResConfig;
import com.github.dr.rwserver.net.web.realization.def.UrmAndUrl;
import com.github.dr.rwserver.net.web.realization.i.RequestManager;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;
import java.util.Map;

public class MyData extends MySon {
    Map<String, String> urlMap = ResConfig.get().getMyEntity();

    public boolean my(List<FileAndName> fileAndNames, RequestManager http, ChannelHandlerContext ch, String url) {
        Class<?> c;
        boolean isRight = true;
        try {
            //从uri里将实例化的类截取出来
            UrmAndUrl uri = uri(url);
            if (uri != null) {
                c = Class.forName(uri.getUri());
                setMyboss(c);
                //System.out.println("uri=="+uri.getUrl());
                //System.out.println("shareuri=="+share.getUri());
                String urm = url.substring(uri.getUrl().length() - 1);
                isRight = data(fileAndNames, http, ch, urm);
            } else {
                System.out.println("NOT FOUND URL");
                isRight = false;
            }
        } catch (Exception e) {
            isRight = false;
        }
        return isRight;
    }

    public UrmAndUrl uri(String uri) {
        UrmAndUrl urmAndUrl = null;
        for (Map.Entry<String, String> entry : urlMap.entrySet()) {
            if (uri.contains(entry.getKey())) {
                urmAndUrl = new UrmAndUrl();
                urmAndUrl.setUri(entry.getValue());
                urmAndUrl.setUrl(entry.getKey());
                break;
            }
        }
        return urmAndUrl;
    }
}
