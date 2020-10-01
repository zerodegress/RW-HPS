package com.github.dr.rwserver.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Dr
 */
public abstract class ReExp {

    private int retryfreq = 0;

    /** 重试的睡眠时间 */
    private int sleepTime = 0;

    private boolean isException = true;

    private Class exception = Exception.class;

    private final Map<Class, String> ClassExpResult = new ConcurrentHashMap<Class, String>();

    public ReExp setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
        return this;
    }

    public ReExp setRetryFreq(int retryfreq) {
        this.retryfreq = retryfreq;
        return this;
    }

    public ReExp setException(Class exception) {
        this.isException = true;
        this.exception = exception;
        return this;
    }

    public ReExp addException(Class exception,String result) {
        ClassExpResult.put(exception,result);
        return this;
    }

    /**
     * 重试
     * @return ?
     */
    protected abstract Object runs() throws Exception;

    /**
     * 默认
     * @return ?
     */
    protected abstract Object defruns();


    public Object execute() {
        for (int i = 0; i < retryfreq; i++) {
            try {
                return runs();
            } catch (Exception e) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException interruptedException) {
                }
            }
        }
        return defruns();
    }

    public Object countExecute(String name) {
        ResultData data = new ResultData(name,retryfreq);
        for (int i = 0; i < retryfreq; i++) {
            try {
                data.result = runs();
                return data;
            } catch (Exception e) {
                if (isException && exception.isInstance(e)) {
                    data.failures++;
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException interruptedException) {
                    }
                } else {
                    Iterator it = ClassExpResult.entrySet().iterator();
                    while(it.hasNext()){
                        Entry entry = (Entry)it.next();
                        Class classdata = (Class) entry.getKey();
                        if (classdata.isInstance(e)) {
                            data.result = entry.getValue();
                            return data;
                        }
                    }
                    return defruns();
                }
            }
        }
        return defruns();
    }

    public static class ResultData {
        public Object result = null;
        public int cout = 0;
        public int failures = 0;
        public ResultData(String name,int rq) {
            cout = rq;
        }
    }

}