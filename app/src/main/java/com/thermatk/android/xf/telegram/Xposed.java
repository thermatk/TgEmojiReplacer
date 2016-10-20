package com.thermatk.android.xf.telegram;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getStaticIntField;

public class Xposed implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (!"org.telegram.messenger".equals(loadPackageParam.packageName)) {
            return;
        }

        final Class<?> EmojiClass = findClass("org.telegram.messenger.Emoji", loadPackageParam.classLoader);


        XC_MethodReplacement replaceEmoji_methodReplacement = new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                return methodHookParam.args[0];
            }
        };
        findAndHookMethod(EmojiClass,
                "replaceEmoji", CharSequence.class, Paint.FontMetricsInt.class, int.class, boolean.class, int[].class,
                replaceEmoji_methodReplacement);

        try {
            final Class<?> EmojiGridAdapter = findClass("org.telegram.ui.Components.EmojiView.EmojiGridAdapter", loadPackageParam.classLoader);

            findAndHookMethod(EmojiGridAdapter, "getView", int.class, View.class, ViewGroup.class,
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
                            Object o = XposedBridge.invokeOriginalMethod(methodHookParam.method, methodHookParam.thisObject, methodHookParam.args);
                            if (o instanceof ImageView) {
                                ImageView imageView = (ImageView) o;
                                String convert = (String) imageView.getTag();
                                try {

                                    int bigImgSize = getStaticIntField(EmojiClass, "bigImgSize");
                                    imageView.setImageDrawable(TextDrawable.builder()
                                            .beginConfig()
                                            .textColor(Color.BLACK)
                                            .fontSize(bigImgSize)
                                            .endConfig()
                                            .buildRect(convert, Color.TRANSPARENT));
                                } catch (Exception ignored) {
                                    imageView.setImageDrawable(TextDrawable.builder()
                                            .beginConfig()
                                            .textColor(Color.BLACK)
                                            .endConfig()
                                            .buildRect(convert, Color.TRANSPARENT));

                                }

                            }
                            return o;
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
