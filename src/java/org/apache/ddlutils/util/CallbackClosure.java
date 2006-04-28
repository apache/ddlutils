package org.apache.ddlutils.util;

/*
 * Copyright 2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.collections.Closure;
import org.apache.ddlutils.DdlUtilsException;

/**
 * A closure that determines a callback for the type of the object and calls it.
 * Note that inheritance is also taken into account. I.e. if the object is of
 * type B which is a subtype of A, and there is only a callback for type A,
 * then this one will be invoked. If there is however also a callback for type B,
 * then only this callback for type B will be invoked and not the one for type A. 
 * 
 * @version $Revision: $
 */
public class CallbackClosure implements Closure
{
    /** The object on which the callbacks will be invoked. */
    private Object _callee;
    /** The cached callbacks. */
    private Map _callbacks = new HashMap();

    /**
     * Creates a new closure object.
     * 
     * @param callee       The object on which the callbacks will be invoked
     * @param callbackName The name of the callback method
     */
    public CallbackClosure(Object callee, String callbackName)
    {
        _callee = callee;

        Class type = callee.getClass();

        // we're caching the callbacks
        do
        {
            Method[] methods = type.getMethods();

            if (methods != null)
            {
                for (int idx = 0; idx < methods.length; idx++)
                {
                    if (methods[idx].getName().equals(callbackName) &&
                        (methods[idx].getParameterTypes() != null) &&
                        (methods[idx].getParameterTypes().length == 1))
                    {
                        _callbacks.put(methods[idx].getParameterTypes()[0], methods[idx]);
                    }
                }
            }
            type = type.getSuperclass();
        }
        while ((type != null) && !type.equals(Object.class));
    }

    /**
     * {@inheritDoc}
     */
    public void execute(Object obj) throws DdlUtilsException
    {
        Queue queue = new LinkedList();

        queue.add(obj.getClass());
        while (!queue.isEmpty())
        {
            Class  type     = (Class)queue.poll();
            Method callback = (Method)_callbacks.get(type);

            if (callback != null)
            {
                try
                {
                    callback.invoke(_callee, new Object[] { obj });
                    return;
                }
                catch (InvocationTargetException ex)
                {
                    throw new DdlUtilsException(ex.getTargetException());
                }
                catch (IllegalAccessException ex)
                {
                    throw new DdlUtilsException(ex);
                }
            }
            if ((type.getSuperclass() != null) && !type.getSuperclass().equals(Object.class))
            {
                queue.add(type.getSuperclass());
            }

            Class[] baseInterfaces = type.getInterfaces();

            if (baseInterfaces != null)
            {
                for (int idx = 0; idx < baseInterfaces.length; idx++)
                {
                    queue.add(baseInterfaces[idx]);
                }
            }
        }
    }
}
