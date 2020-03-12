package com.qmai.android.loadgradleplugin;

import com.android.SdkConstants;
import com.android.build.api.transform.Context;
import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;

/**
 * created by wangwei   ON 3/4/20  email:wangwei_5521@163.com
 *
 * @version 1.1.1
 * @Description
 **/
public class CoreTransform extends Transform {
    @Override
    public String getName() {
        return "instance";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(Context context, Collection<TransformInput> inputs
            , Collection<TransformInput> referencedInputs
            , TransformOutputProvider outputProvider
            , boolean isIncremental) throws IOException, TransformException, InterruptedException {
        Set<String> implInfoClasses = new HashSet<>();

        for (TransformInput input : inputs) {
            // 第三方jar
            input.getJarInputs().forEach(jarInput -> {
                File jarFile = jarInput.getFile();

                File dst = outputProvider.getContentLocation(
                        jarInput.getName(), jarInput.getContentTypes(), jarInput.getScopes(),
                        Format.JAR);
                /// -------------
                try {
                    JarFile file = new JarFile(jarFile);
                    Enumeration<JarEntry> entries = file.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        String name = entry.getName();
                        System.out.println("<< jar ---" + name +""+(name.endsWith(".class") && name.startsWith("ImplInfo_")));
                        if (name.endsWith(".class") && name.startsWith("com/qmai/android/instance/ImplInfo_")) {
                            String className = name.substring(0, name.lastIndexOf(".")).replace("/", ".");
                            System.out.println("<< jar 找到class  = " + className + ">>");
                            implInfoClasses.add(className);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    FileUtils.copyFile(jarFile, dst);   //必须要把输入，copy到输出，不然接下来没有办法处理
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            // 工程目录
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                File dir = directoryInput.getFile();

                File packageDir = new File(dir, "com/qmai/android/instance");
                if (packageDir.exists() && packageDir.isDirectory()) {
                    Collection<File> files = FileUtils.listFiles(packageDir,
                            new SuffixFileFilter(SdkConstants.DOT_CLASS, IOCase.INSENSITIVE), TrueFileFilter.INSTANCE);
                    files.forEach(match -> {
                        String className = trimName(match.getAbsolutePath(), dir.getAbsolutePath().length() + 1).replace(File.separatorChar, '.');
                        System.out.println("<< dir 找到class  = " + className + ">>");
                        implInfoClasses.add(className);
                    });
                }
                File dst = outputProvider.getContentLocation(
                        directoryInput.getName(), directoryInput.getContentTypes(),
                        directoryInput.getScopes(), Format.DIRECTORY);
                FileUtils.copyDirectory(dir, dst);
            }
        }

        File dest = outputProvider.getContentLocation(
                "ImplLoader", TransformManager.CONTENT_CLASS,
                ImmutableSet.of(QualifiedContent.Scope.PROJECT), Format.DIRECTORY);
        if (!implInfoClasses.isEmpty()) {
            /**
             *
             *  Asm 生成 class
             * public class NewInstanceHelper{
             *   public static void init(){
             *       ImplInfo_XXX.init();
             *       ......
             *       .....more code
             *   }
             * }
             */
            // 生成代码
            byte[] byteCode = generateCode(implInfoClasses);
            File save = new File(dest.getAbsolutePath(), "com/qmai/getinstance/loaderhelper/NewInstanceHelper" + SdkConstants.DOT_CLASS);
            System.out.println("----文件目录----" + save.getAbsolutePath());
            save.getParentFile().mkdirs();
            new FileOutputStream(save).write(byteCode);

            System.out.println("----生成代码结束----");
        }
    }

    /**
     * [prefix]com/xxx/aaa.class --> com/xxx/aaa
     * [prefix]com\xxx\aaa.class --> com\xxx\aaa
     */
    private static String trimName(String s, int start) {
        return s.substring(start, s.length() - SdkConstants.DOT_CLASS.length());
    }


    private byte[] generateCode(Set<String> implClasses) {
        System.out.println("----开始生成代码----");
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        MethodVisitor mv;
        cw.visit(52, ACC_PUBLIC + ACC_SUPER,
                "com/qmai/getinstance/loaderhelper/NewInstanceHelper", null, "java/lang/Object", null);
        cw.visitSource("NewInstanceHelper.java", null);
        // 默认构造函数
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(5, l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("this", "Lcom/qmai/getinstance/loaderhelper/NewInstanceHelper;", null, l0, l1, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        //
        mv = cw.visitMethod(ACC_PUBLIC + Opcodes.ACC_STATIC, "init", "()V", null, null);
        mv.visitCode();
        Iterator<String> iterable = implClasses.iterator();
        while (iterable.hasNext()) {
            String implClass = iterable.next();
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, implClass.replace(".", "/"), "init", "()V", false);
        }
        mv.visitInsn(RETURN);
        mv.visitEnd();
        cw.visitEnd();


        return cw.toByteArray();

    }

}
