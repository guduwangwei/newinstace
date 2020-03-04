package com.qmai.android.newinstance_compiler;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * created by wangwei   ON 3/2/20  email:wangwei_5521@163.com
 *
 * @version 1.1.1
 * @Description
 **/

public abstract class BaseProcessor extends AbstractProcessor {
    protected Elements elements;
    protected Filer filer;

    protected Logger logger;
    private Types types;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elements = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        logger = new Logger(processingEnv.getMessager());
        types = processingEnv.getTypeUtils();

    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

}
