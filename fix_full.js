const fs = require('fs');
const filePath = 'src/com/supertech/BoltWS/BoltWS.java';

const fullCode = `package com.supertech.BoltWS;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.? ?????? ??? ???? ??? ?? ? `add_target_selector.js` ?????? `setupTargetViewsList()` ?? `getSelectedTargetView()` ? ??? ????? ?????? ???? ??? ?? ? ????? ? ??????? ????? ????? ?? ???, ?? ??? ?? ??? ?? ?? ??.

??? ?? ?????? ? ????? ???? ??? `undefined method` ? ??? ?? ??.

? ???? ????? ??????? ?? ??? ???? ??? ?? ???? ?? ?????? ??? ?????? ??? **??? ?? ???** PowerShell ??????? ??? ??? **Enter** ??:

```powershell
Set-Content -Path "fix_missing_methods.js" -Value @'
const fs = require('fs');
const path = 'src/com/supertech/BoltWS/BoltWS.java';
let code = fs.readFileSync(path, 'utf8');

// ????? ???? ???? ? ???
if (!code.includes('availableTargetViews')) {
    code = code.replace(/private Spinner methodSpinner = null;/, 
        'private Spinner methodSpinner = null;\n    private Spinner targetViewSpinner = null;\n    private final List<View> availableTargetViews = new ArrayList<View>();\n    private final List<String> availableTargetNames = new ArrayList<String>();');
}

// ??? ??? ??? ????? ??? ???
const missingFunctions = `
    private void setupTargetViewsList() {
        if (targetViewSpinner == null || defaultTargetComponent == null) return;
        availableTargetViews.clear();
        availableTargetNames.clear();

        View mainView = defaultTargetComponent.getView();
        if (mainView != null) {
            availableTargetViews.add(mainView);
            availableTargetNames.add("Target: Root Layout (" + mainView.getClass().getSimpleName() + ")");

            if (mainView instanceof ViewGroup) {
                findChildViews((ViewGroup) mainView, "Child");
            }
        }

        ArrayAdapter<String> targetAdapter = new ArrayAdapter<String>(
                activity, android.R.layout.simple_spinner_item, availableTargetNames);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        targetViewSpinner.setAdapter(targetAdapter);
    }

    private void findChildViews(ViewGroup group, String prefix) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            String name = prefix + " " + (i + 1) + ": " + child.getClass().getSimpleName();
            availableTargetViews.add(child);
            availableTargetNames.add(name);
            if (child instanceof ViewGroup) {
                findChildViews((ViewGroup) child, prefix + "." + (i + 1));
            }
        }
    }

    private Object getSelectedTargetView(Class<?> expectedType) {
        View selected = null;
        if (targetViewSpinner != null && targetViewSpinner.getSelectedItemPosition() >= 0 && targetViewSpinner.getSelectedItemPosition() < availableTargetViews.size()) {
            selected = availableTargetViews.get(targetViewSpinner.getSelectedItemPosition());
        }
        if (selected == null && defaultTargetComponent != null) {
            selected = defaultTargetComponent.getView();
        }

        if (AndroidViewComponent.class.isAssignableFrom(expectedType)) {
            return defaultTargetComponent;
        }
        return selected;
    }
`;

// executeSelectedMethodFromUI ?? ??? ?? ??????? ???
if (!code.includes('void setupTargetViewsList()')) {
    code = code.replace(/private void executeSelectedMethodFromUI\(\)/, missingFunctions + '\n    private void executeSelectedMethodFromUI()');
}

fs.writeFileSync(path, code, 'utf8');
console.log(' ??? ??? ????? BoltWS.java ??? ??????? ??? ?? ???!');
