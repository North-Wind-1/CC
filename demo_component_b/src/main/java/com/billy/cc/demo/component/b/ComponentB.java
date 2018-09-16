package com.billy.cc.demo.component.b;

import com.billy.cc.core.component.CC;
import com.billy.cc.core.component.CCResult;
import com.billy.cc.core.component.IComponent;
import com.billy.cc.demo.component.b.processor.IActionProcessor;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * demo组件B
 * 这里示例用ActionProcessor来处理action，避免if/else分支太多
 * 这里的ActionProcessor注册可以使用auto-register来实现自动注册
 * 可以选择的注册方式有：
 *  1. 在static块、普通代码块、构造方法中注册： 程序启动后第一次执行CC时即进行初始化及注册
 *  2. 在普通方法中注册： 可以自定义调用时机
 *  本类示例的是第2种方式，在onCall方法中注册，在组件第一次被调用时才注册，类似于懒加载
 *
 *      自定义cc-settings.gradle, 在autoregister.registerInfo中添加配置，例如(cc-settings-demo.gradle)：
 *
     autoregister {
         registerInfo = [
             [
             'scanInterface'             : 'com.billy.cc.core.component.IComponent'
             , 'codeInsertToClassName'   : 'com.billy.cc.core.component.ComponentManager'
             , 'registerMethodName'      : 'registerComponent'
             ],
             [
             'scanInterface'             : 'com.billy.cc.demo.component.b.processor.IActionProcessor'
             , 'codeInsertToClassName'   : 'com.billy.cc.demo.component.b.ComponentB'
             , 'codeInsertToMethodName'  : 'initProcessors'
             , 'registerMethodName'      : 'add'
             ]
         ]
     }
 * @author billy.qi
 * @since 17/11/20 21:00
 */
public class ComponentB implements IComponent {

    private AtomicBoolean initialized = new AtomicBoolean(false);
    private final HashMap<String, IActionProcessor> map = new HashMap<>(4);

    private void initProcessors() {
    }

    private void add(IActionProcessor processor) {
        map.put(processor.getActionName(), processor);
    }

    @Override
    public String getName() {
        return "ComponentB";
    }

    @Override
    public boolean onCall(CC cc) {
        if (initialized.compareAndSet(false, true)) {
            synchronized (map) {
                initProcessors();
            }
        }
        String actionName = cc.getActionName();
        IActionProcessor processor = map.get(actionName);
        if (processor != null) {
            return processor.onActionCall(cc);
        }
        CC.sendCCResult(cc.getCallId(), CCResult.error("has not support for action:" + cc.getActionName()));
        return false;
    }

}
