package com.fzm.chat33.core.provider;

/**
 * @author zhengjy
 * @since 2018/12/21
 * Description:获取显示需要的头像和昵称
 */
public class InfoProvider {

    private ProvideStrategy strategy;

    private static class InfoProviderHolder {
        private static InfoProvider sInstance = new InfoProvider();
    }

    public static InfoProvider getInstance() {
        return InfoProviderHolder.sInstance;
    }

    public <T> InfoProvider strategy(ProvideStrategy<T> strategy) {
        this.strategy = strategy;
        return this;
    }

    @SuppressWarnings("unchecked")
    public void load(OnFindInfoListener listener) {
        if (strategy != null) {
            strategy.load(listener);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz) {
        if (strategy == null) {
            return null;
        }
        return (T) strategy.get();
    }
}
