/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.gcsc.vrl.langsupport.lua;

import edu.gcsc.vrl.ug.Pointer;
import edu.gcsc.vrl.ug.UGObject;
import edu.gcsc.vrl.ug.UGObjectInterface;
import edu.gcsc.vrl.ug.api.C_void;
import edu.gcsc.vrl.ug.api.Const__C_void;
import edu.gcsc.vrl.ug.api.I_LuaShell;
import edu.gcsc.vrl.ug.api.LuaShell;
import eu.mihosoft.vrl.annotation.ComponentInfo;
import eu.mihosoft.vrl.annotation.OutputInfo;
import eu.mihosoft.vrl.annotation.ParamInfo;
import eu.mihosoft.vrl.io.IOUtil;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.OperationNotSupportedException;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
@ComponentInfo(name = "UG Lua Shell", category = "UG4/Lua")
public class UGLuaShell implements Serializable {

    private static final long serialVersionUID = 1L;

    private transient I_LuaShell shell;

    public void setWorkingDir(@ParamInfo(name = "value", style = "load-folder-dialog") File wd) throws OperationNotSupportedException {
        throw new OperationNotSupportedException("Currently there is no way to change UGs CWD from Java.");
    }

    public void set(
            @ParamInfo(name = "name") String name,
            @ParamInfo(name = "value") String value) {
        getRawShell().set(name, value);
    }

    public void setFile(
            @ParamInfo(name = "name") String name,
            @ParamInfo(name = "value", style = "load-dialog") File value) {
        getRawShell().set(name, value.getAbsolutePath());
    }

    public void setFolder(
            @ParamInfo(name = "name") String name,
            @ParamInfo(name = "value", style = "load-folder-dialog") File value) {
        getRawShell().set(name, value.getAbsolutePath());
    }

    public void set(
            @ParamInfo(name = "name") String name,
            @ParamInfo(name = "value") double value) {
        getRawShell().set(name, value);
    }

    public void set(
            @ParamInfo(name = "name") String name,
            @ParamInfo(name = "value") boolean value) {
        getRawShell().set(name, value);
    }

    public void set(
            @ParamInfo(name = "name") String name,
            @ParamInfo(name = "value") UGObjectInterface value) {
        setVoid(name, (UGObject) value);
    }

    public void set(
            @ParamInfo(name = "name") String name,
            @ParamInfo(name = "value") UGObjectInterface value,
            @ParamInfo(name = "const") boolean isConst) {
//        setVoid(name, value, isConst);
    }

    public String getString(@ParamInfo(name = "name") String name) {
        return getRawShell().get_string(name);
    }

    public double getNumber(@ParamInfo(name = "name") String name) {
        return getRawShell().get_number(name);
    }

    public boolean getBoolean(@ParamInfo(name = "name") String name) {
        return getRawShell().get_bool(name);
    }

    public void run(@ParamInfo(name = "code", style = "lua-code") String code) {
        getRawShell().run(code);
    }

    public void run(@ParamInfo(name = "code", style = "load-dialog",
            options="endings=[\".txt\",\".lua\",\".uglua\"]; description=\"Lua Files (*.lua, *.txt, *.uglua)\"")
            File code) throws IOException {
        getRawShell().run(new String(IOUtil.fileToByteArray(code), "UTF-8"));
    }

    private void setVoid(String name, UGObject obj) {

        try {

            Method getPointer = UGObject.class.getDeclaredMethod("getPointer");
            getPointer.setAccessible(true);

            Pointer pointer = (Pointer) getPointer.invoke(obj);

            if (obj.getClass().getSimpleName().startsWith("Const__")) {
                Const__C_void result = new Const__C_void();

                Field resultPointer = UGObject.class.getDeclaredField("objPointer");
                resultPointer.setAccessible(true);

                resultPointer.set(result, new Pointer("c_void", pointer.getAddress(), true));
                getRawShell().set(name, result, obj.getClassName());
                System.out.println("calling const: " + result);
            } else {
                C_void result = new C_void();

                Field resultPointer = UGObject.class.getDeclaredField("objPointer");
                resultPointer.setAccessible(true);

                resultPointer.set(result, new Pointer("c_void", pointer.getAddress(), false));
                getRawShell().set(name, result, obj.getClassName());
                System.out.println("calling non-const: " + result);
            }

        } catch (NoSuchFieldException ex) {
            Logger.getLogger(UGLuaShell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(UGLuaShell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(UGLuaShell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(UGLuaShell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(UGLuaShell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(UGLuaShell.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setVoidAsConst(String name, UGObject obj) {

        try {

            Method getPointer = UGObject.class.getDeclaredMethod("getPointer");
            getPointer.setAccessible(true);

            Pointer pointer = (Pointer) getPointer.invoke(obj);

            Const__C_void result = new Const__C_void();

            Field resultPointer = UGObject.class.getDeclaredField("objPointer");
            resultPointer.setAccessible(true);

            resultPointer.set(result, new Pointer("c_void", pointer.getAddress(), true));
            getRawShell().set(name, result, obj.getClassName());
            System.out.println("calling non-const -> const: " + result);

        } catch (NoSuchFieldException ex) {
            Logger.getLogger(UGLuaShell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(UGLuaShell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(UGLuaShell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(UGLuaShell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(UGLuaShell.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(UGLuaShell.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @OutputInfo(name = "LuaShell")
    public I_LuaShell getRawShell() {
        if (shell == null) {
            shell = new LuaShell();
        }

        return shell;
    }
}