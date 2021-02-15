package com.github.dr.rwserver.net.web.realization;

import com.github.dr.rwserver.net.web.realization.agreement.ShareMessage;
import com.github.dr.rwserver.net.web.realization.def.ResConfig;
import com.github.dr.rwserver.net.web.realization.def.UrmAndUrl;
import com.github.dr.rwserver.net.web.realization.i.RequestManager;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;

public class MyControl extends MySon {
    Map<String, String> urlMap = ResConfig.get().getMyEntity();

    public boolean my(ShareMessage share, RequestManager http, ChannelHandlerContext ch) {
        Class<?> c;
        boolean isRight = true;
        try {
            //从uri里将实例化的类截取出来
            UrmAndUrl uri = uri(share.getUri());
            if (uri != null) {
                c = Class.forName(uri.getUri());
                setMyboss(c);
                String urm = share.getUri().substring(uri.getUrl().length() - 1);
                isRight = body(share.getBody(), share.getParams(), http, ch, urm);
            } else {
                isRight = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            isRight = false;
        }
        return isRight;
    }

    public UrmAndUrl uri(String uri) {
        UrmAndUrl urmAndUrl = null;
        for (Map.Entry<String, String> entry : urlMap.entrySet()) {
            if (uri.indexOf(entry.getKey()) > -1) {
                urmAndUrl = new UrmAndUrl();
                urmAndUrl.setUri(entry.getValue());
                urmAndUrl.setUrl(entry.getKey());
                break;
            }
        }
        return urmAndUrl;
    }
}