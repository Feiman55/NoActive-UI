package cn.myflv.noactive.core.hook.miui;

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
    
    private final static Map<Integer, Long> lastThawMap = new ConcurrentHashMap<>();

    private final static String REASON = "received sync binder";
    //定义的ASON是对应异步解冻测试的，可以删除
    private final static String ASON = "received oneway binder";
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
                // 是否异步
                boolean isOneway = (boolean) args[5];
                if (isOneway) {
                    
                long currentTime = System.currentTimeMillis();

                //获取上次解冻时间戳，如果没有解冻就是0

                long lastThawTime = lastThawMap.computeIfAbsent(uid, k -> 0L);

                //如果当前时间-上次解冻时间，小于60秒就return返回

                if (currentTime - lastThawTime < 60 * 1000 ){

                return;

                }

                //存入当前时间

                lastThawMap.put(uid, currentTime);
                    // 异步不处理，下边两行是异步解冻可以删除，目前是测试用的
                   // Log.i("这是异步binder解冻处理");
                    freezerHandler.temporaryUnfreezeIfNeed(uid, ASON);
                    return;
                }
                freezerHandler.temporaryUnfreezeIfNeed(uid, REASON);
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
