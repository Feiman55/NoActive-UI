package cn.myflv.noactive.core.hook.miui;

import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

import cn.myflv.noactive.constant.ClassConstants;

import cn.myflv.noactive.constant.MethodConstants;

import cn.myflv.noactive.core.handler.FreezerHandler;

import cn.myflv.noactive.core.hook.base.AbstractMethodHook;

import cn.myflv.noactive.core.hook.base.MethodHook;

import de.robv.android.xposed.XC_MethodHook;

/**

 * Binder通信Hook.

 */

public class BinderTransHook extends MethodHook {

    /**

     * 上一次解冻时间Map.

     */

    private final static Map<Integer, Long> lastThawMap = new ConcurrentHashMap<>();

    private final static String SYNC_REASON = "received sync binder";

    private final static String ASYNC_REASON = "received async binder";

    private final static int ASYNC_INTERVAL = 60 * 1000;

    /**

     * 应用切换Hook

     */

    private final FreezerHandler freezerHandler;

    public BinderTransHook(ClassLoader classLoader, FreezerHandler freezerHandler) {

        super(classLoader);

        this.freezerHandler = freezerHandler;

    }

    @Override

    public String getTargetClass() {

        return ClassConstants.GreezeManagerService;

    }

    @Override

    public String getTargetMethod() {

        return MethodConstants.reportBinderTrans;

    }

    @Override

    public Object[] getTargetParam() {

        return new Object[]{int.class, int.class, int.class, int.class, int.class, boolean.class, long.class, int.class};

    }

    @Override

    public XC_MethodHook getTargetHook() {

        return new AbstractMethodHook() {

            @Override

            protected void beforeMethod(MethodHookParam param) throws Throwable {

                Object[] args = param.args;

                int uid = (int) args[0];

                //获取当前时间戳

                long currentTime = System.currentTimeMillis();

                // 是否异步

                boolean isOneway = (boolean) args[5];

                if (isOneway) {

                    //获取上次解冻时间戳，如果没有解冻就是0

                    Long lastThawTime = lastThawMap.computeIfAbsent(uid, k -> 0L);

                    //如果当前时间-上次解冻时间，小于60秒就return返回

                    if (currentTime - lastThawTime < ASYNC_INTERVAL) {

                        return;

                    }

                }

                //存入当前时间

                lastThawMap.put(uid, currentTime);

                freezerHandler.temporaryUnfreezeIfNeed(uid, isOneway ? ASYNC_REASON : SYNC_REASON);

            }

        };

    }

    @Override

    public int getMinVersion() {

        return ANY_VERSION;

    }

    @Override

    public String successLog() {

        return "Perfect Freezer";

    }

    @Override

    public boolean isIgnoreError() {

        return true;

    }

}

