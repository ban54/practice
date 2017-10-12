package viewbind.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import viewbind.annotation.Bind;
import viewbind.annotation.OnClick;

/**
 * Created by LIHAO on 2017/9/29.
 */
@AutoService(Processor.class)
public class ViewBindProcessor extends AbstractProcessor {
    private Elements mElements;
    private Filer mFiler;
    private Types mTypes;
    private Messager mMessager;

    private HashMap<String, HashMap<String, List<BindParams>>> mBindMap;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mElements = processingEnvironment.getElementUtils();
        mFiler = processingEnvironment.getFiler();
        mTypes = processingEnvironment.getTypeUtils();
        mMessager = processingEnvironment.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        mBindMap = new HashMap<>();
        Set<? extends Element> bindSet = roundEnvironment.getElementsAnnotatedWith(Bind.class);
        for (Element element : bindSet) {
            if (element.getKind() == ElementKind.FIELD) {
                processBind(element);
            }
        }
        Set<? extends Element> onclickSet = roundEnvironment.getElementsAnnotatedWith(OnClick.class);
        for (Element element : onclickSet) {
            if (element.getKind() == ElementKind.METHOD) {
                processOnClick(element);
            }
        }

        try {
            for (String packageName : mBindMap.keySet()) {
                HashMap<String, List<BindParams>> map = mBindMap.get(packageName);
                for (String clsName : map.keySet()) {
                    List<BindParams> paramList = map.get(clsName);

                    // 构造方法
                    MethodSpec.Builder methodSpec = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
                    List<ParameterSpec> parameter = new ArrayList<>();
                    parameter.add(ParameterSpec.builder(ClassName.get("android.app", "Activity"), "activity").build());
                    parameter.add(ParameterSpec.builder(ClassName.get("android.view", "View"), "view").build());
                    methodSpec.addParameters(parameter);
                    // 方法内代码
                    methodSpec.addCode("final " + clsName + " instance = (" + clsName + ") activity;");
                    for (BindParams params : paramList) {
                        if (params instanceof VariableParams) {
                            VariableParams p = (VariableParams) params;
                            methodSpec.addCode("instance.$L = ($L) view.findViewById($L);", p.name, p.type, p.id);
                        } else if (params instanceof OnClickParams) {
                            OnClickParams p = (OnClickParams) params;
                            for (int id : p.ids) {
                                methodSpec.addCode("view.findViewById($L).setOnClickListener(new android.view.View.OnClickListener() {"
                                        + "\n\tpublic void onClick(android.view.View view) {\n\tinstance."
                                        + p.name + "(view.findViewById($L));}});", id, id);
                            }
                        }
                    }

                    JavaFile javaFile = JavaFile.builder(packageName,
                            TypeSpec.classBuilder(clsName.substring(packageName.length() + 1) + "_ViewBind")
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(methodSpec.build()).build()).build();
                    javaFile.writeTo(mFiler);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void processBind(Element element) {
        String packageName = getPackageName(element);
        String className = getClassName(element);
        HashMap<String, List<BindParams>> map = mBindMap.get(packageName);
        if (map == null) {
            map = new HashMap<>();
            mBindMap.put(packageName, map);
        }
        List<BindParams> paramsList = map.get(className);
        if (paramsList == null) {
            paramsList = new ArrayList<>();
            map.put(className, paramsList);
        }

        Bind bind = element.getAnnotation(Bind.class);
        VariableParams params = new VariableParams();
        params.id = bind.value();
        params.name = element.getSimpleName().toString();
        params.type = element.asType().toString();
        paramsList.add(params);
    }

    private void processOnClick(Element element) {
        String packageName = getPackageName(element);
        String className = getClassName(element);
        HashMap<String, List<BindParams>> map = mBindMap.get(packageName);
        if (map == null) {
            map = new HashMap<>();
            mBindMap.put(packageName, map);
        }
        List<BindParams> paramsList = map.get(className);
        if (paramsList == null) {
            paramsList = new ArrayList<>();
            map.put(className, paramsList);
        }

        OnClick onclick = element.getAnnotation(OnClick.class);
        int[] ids = onclick.value();
        OnClickParams params = new OnClickParams();
        params.ids = ids;
        params.name = element.getSimpleName().toString();
        paramsList.add(params);
    }

    private String getPackageName(Element type) {
        return mElements.getPackageOf(type).getQualifiedName().toString();
    }

    private static String getClassName(Element type) {
        return ((TypeElement) type.getEnclosingElement()).getQualifiedName().toString();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new LinkedHashSet<>();
        set.add(Bind.class.getCanonicalName());
        set.add(OnClick.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}
