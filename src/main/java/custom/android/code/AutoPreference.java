package custom.android.code;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves the state of the object.
 */
public class AutoPreference {
    public static final String TAG = AutoPreference.class.getSimpleName();
    public static boolean debug = true;

    String namespace;
    SharedPreferences sharedPrefs;
    SharedPreferences.Editor editor;
    List<Field> mFields = new ArrayList<Field>();
    Context context;
    Object object;

    public AutoPreference(Context context, String namespace, int mode) {
        sharedPrefs = context.getSharedPreferences(namespace, mode);
        this.namespace = namespace;
        this.context = context;
    }

    /** analyzes the object in search of annotated fields */
    public void parser(Object object) {
        this.object = object;
        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(PreferenceStore.class)) {
                if (getPreferenceStore(field).enable()) {
                    mFields.add(field);
                }
            }
        }
    }

    /** restores the state variables */
    public void restore() {
        assertParserCall(object == null);

        Class preferenceClass = SharedPreferences.class;

        for (Field field : mFields) {
            Class<?> fieldType = field.getType();
            String getMethodName = genMethodName("get", fieldType);
            String keyName = genKeyName(getPreferenceStore(field));
            try {
                Method method = preferenceClass.getDeclaredMethod(getMethodName,
                        String.class, fieldType);

                Object value = field.get(object); // get default field value.

                Object fieldValue = method.invoke(sharedPrefs, keyName, value);

                if (fieldValue != null) {  // update field
                    field.set(object, fieldValue);
                }
            } catch (IllegalAccessException e) {
                if (debug) Log.d(TAG, "- " + keyName, e);

            } catch (InvocationTargetException e) {
                if (debug) Log.d(TAG, "- " + keyName, e);

            } catch (NoSuchMethodException e) {
                if (debug) Log.d(TAG, "- " + keyName, e);
            }
        }
    }

    protected SharedPreferences.Editor getEdit() {
        if (editor == null) {
            editor = sharedPrefs.edit();
        }
        return editor;
    }

    protected String genMethodName(String prefix, Class fieldType) {
        return prefix + prepareTypeName(fieldType.getSimpleName());
    }

    private String genKeyName(PreferenceStore preferenceStore) {
        return namespace + ":" + preferenceStore.name();
    }

    protected String prepareTypeName(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    /** Name passed as a parameter for annotations to be used as a key field */
    protected PreferenceStore getPreferenceStore(Field field) {
        return field.getAnnotation(PreferenceStore.class);
    }

    protected void assertParserCall(boolean condition) {
        if (condition) throw new RuntimeException(
                String.format("Need to call 'parser' on '%s' first!", TAG));
    }

    /** Saves the current state of the object */
    public void commit() {
        assertParserCall(object == null);

        SharedPreferences.Editor _editor = getEdit();
        Class editorClass = SharedPreferences.Editor.class;

        try {
            for (Field field : mFields) {
                Class fieldType = field.getType();
                String putMethodName = genMethodName("put", fieldType);
                String keyName = genKeyName(getPreferenceStore(field));
                try {
                    Object value = field.get(object);

                    Method method = editorClass.getDeclaredMethod(putMethodName,
                            String.class, fieldType);

                    method.invoke(_editor, keyName, value);
                } catch (IllegalAccessException e) {
                    if (debug) Log.d(TAG, "- " + keyName, e);

                } catch (NoSuchMethodException e) {
                    if (debug) Log.d(TAG, "- " + keyName, e);

                } catch (InvocationTargetException e) {
                    if (debug) Log.d(TAG, "- " + keyName, e);
                }
            }
        } finally {
            _editor.commit();
        }
    }
}
