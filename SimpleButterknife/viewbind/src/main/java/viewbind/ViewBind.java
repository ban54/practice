package viewbind;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.Constructor;

/**
 * Created by LIHAO on 2017/9/29.
 */
public class ViewBind {

    public static void bind(Activity activity) {
        View rootView = activity.getWindow().getDecorView();
        try {
            Class cls = Class.forName(activity.getClass().getCanonicalName() + "_ViewBind");
            Constructor constructor = cls.getConstructor(Activity.class, View.class);
            constructor.newInstance(activity, rootView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
