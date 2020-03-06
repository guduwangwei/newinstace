package com.qmai.android.newinstance_compiler;

import com.google.auto.service.AutoService;
import com.qmai.android.newinstance_annotation.GetInstance;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * created by wangwei   ON 3/2/20  email:wangwei_5521@163.com
 *
 * @version 1.1.1
 * @Description
 **/
@AutoService(Processor.class)
public class GetInstanceProcessor extends BaseProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!(annotations == null || annotations.isEmpty())) {
            Set<? extends Element> instances = roundEnv.getElementsAnnotatedWith(GetInstance.class);
            try {
                parseAnnotations(instances);
            } catch (IOException e) {
                logger.error(e);
            }
            return true;
        }
        return false;
    }

    /**
     * 需要成的文件
     * <p>
     * public class ImplInfo_${path}{
     * public static void init(){
     * ImplLoader.registerImpl("path",.classs);
     * }
     * }
     */
    private void parseAnnotations(Set<? extends Element> routeElements) throws IOException {

        MethodSpec.Builder initBuilder
                = MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        for (Element element : routeElements) {
            GetInstance gT = element.getAnnotation(GetInstance.class);
            String path = gT.path();
            TypeMirror tm = element.asType();
            ClassName implClassName = ClassName.get((TypeElement) element);
            initBuilder.addStatement("$T.registerImpl($S,$T.class)", className("com.qmai.android.newinstance_api.ImplLoader"), path, implClassName);
        }
        TypeSpec typeSpec = TypeSpec.classBuilder("ImplInfo_" + UUID.randomUUID().toString().replace('-', '_'))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(initBuilder.build())
                .build();

        JavaFile.builder("com.qmai.android.instance", typeSpec)
                .build().writeTo(filer);
    }

    /**
     * 从字符串获取ClassName对象
     */
    public ClassName className(String className) {
        TypeElement element = typeElement(className);
        if (element == null) return null;
        return ClassName.get(element);
    }

    /**
     * 从字符串获取TypeElement对象
     */
    public TypeElement typeElement(String className) {
        return elements.getTypeElement(className);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new HashSet<String>() {{
            this.add(GetInstance.class.getCanonicalName());
        }};
    }

}
