package com.supertech.BoltWS;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/** Independent side-navigation and offline library UI. */
final class ProjectLibraryUi {
    interface Snapshot {
        byte[] dex();
        String metadataEnvelope();
        String extensionName();
        String packageName();
        String selectedMethod();
        String arguments();
        String providerInfo();
    }

    private static final int NAV_WIDTH_DP = 128;
    private final Activity activity;
    private final ProjectLibraryRepository repository;
    private final Snapshot snapshot;
    private Dialog dialog;
    private LinearLayout content;
    private boolean favoritesOnly;
    private String searchQuery = "";

    ProjectLibraryUi(Activity activity, ProjectLibraryRepository repository, Snapshot snapshot) {
        this.activity = activity;
        this.repository = repository;
        this.snapshot = snapshot;
    }

    void open() {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setBackgroundColor(Color.WHITE);
        LinearLayout nav = new LinearLayout(activity);
        nav.setOrientation(LinearLayout.VERTICAL);
        nav.setPadding(dp(8), dp(14), dp(8), dp(8));
        nav.setBackgroundColor(Color.rgb(241, 245, 249));
        root.addView(nav, new LinearLayout.LayoutParams(dp(NAV_WIDTH_DP), ViewGroup.LayoutParams.MATCH_PARENT));
        TextView brand = label("BoltWS\nProjects", 18, Color.rgb(15, 23, 42), true);
        brand.setPadding(dp(7), dp(4), dp(4), dp(16));
        nav.addView(brand);
        addNav(nav, "Home", 0);
        addNav(nav, "Project Library", 1);
        addNav(nav, "Test History", 2);
        addNav(nav, "Saved DEX", 3);
        addNav(nav, "Favorites", 4);
        addNav(nav, "Import Project", 5);
        addNav(nav, "Settings", 6);
        View spacer = new View(activity);
        nav.addView(spacer, new LinearLayout.LayoutParams(1, 0, 1));
        addNav(nav, "Close", 7);

        ScrollView scroll = new ScrollView(activity);
        content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(14), dp(16), dp(18));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
        dialog.setContentView(root);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        showHome();
        dialog.show();
        if (window != null) window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void addNav(LinearLayout nav, String title, final int page) {
        Button item = new Button(activity);
        item.setText(title);
        item.setTextSize(11f);
        item.setAllCaps(false);
        item.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        item.setPadding(dp(7), 0, dp(4), 0);
        item.setTextColor(Color.rgb(30, 41, 59));
        item.setBackgroundColor(Color.TRANSPARENT);
        nav.addView(item, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(42)));
        item.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) { navigate(page); }
        });
    }

    private void navigate(int page) {
        if (page == 7) { dialog.dismiss(); return; }
        if (page == 0) showHome();
        else if (page == 1) { favoritesOnly = false; showLibrary(); }
        else if (page == 2) showHistory();
        else if (page == 3) showDexFiles();
        else if (page == 4) { favoritesOnly = true; showLibrary(); }
        else if (page == 5) showImportInfo();
        else if (page == 6) showSettings();
    }

    private void showHome() {
        resetContent("Offline Project Library", "Saved test projects remain available without a PC or WebSocket connection.");
        Button save = action("Save current DEX as Project", Color.rgb(37, 99, 235));
        content.addView(save, matchWrap());
        save.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { promptSave(); } });
        int count = repository.listProjects(false, "").size();
        content.addView(label(count + " saved project(s)", 14, Color.rgb(51, 65, 85), true), matchWrapWithTop(14));
        content.addView(label("This viewer reads saved files only. A saved DEX is not executed offline.", 12, Color.rgb(100, 116, 139), false), matchWrapWithTop(8));
    }

    private void promptSave() {
        final byte[] dex = snapshot.dex();
        if (dex == null || dex.length == 0) { toast("No DEX has been received yet."); return; }
        final EditText name = new EditText(activity);
        name.setSingleLine(true);
        name.setHint("Project name");
        String suggested = snapshot.extensionName();
        name.setText(suggested == null || suggested.trim().isEmpty() ? "Extension project" : suggested);
        new AlertDialog.Builder(activity).setTitle("Save Project").setView(name)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface d, int which) {
                    try {
                        ProjectLibraryRepository.Project saved = repository.saveProject(name.getText().toString(),
                            snapshot.extensionName(), snapshot.packageName(), dex, snapshot.metadataEnvelope(),
                            snapshot.selectedMethod(), snapshot.arguments(), snapshot.providerInfo(), "");
                        toast("Saved " + saved.name);
                        showLibrary();
                    } catch (Exception e) { toast("Save failed: " + e.getMessage()); }
                }
            }).show();
    }

    private void showLibrary() {
        resetContent(favoritesOnly ? "Favorites" : "Project Library", "Saved locally on this device.");
        EditText search = new EditText(activity);
        search.setSingleLine(true);
        search.setHint("Search projects");
        search.setText(searchQuery);
        content.addView(search, matchWrap());
        final LinearLayout list = new LinearLayout(activity);
        list.setOrientation(LinearLayout.VERTICAL);
        content.addView(list, matchWrapWithTop(8));
        renderProjects(list);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString();
                renderProjects(list);
            }
            @Override public void afterTextChanged(Editable e) {}
        });
    }

    private void renderProjects(final LinearLayout list) {
        list.removeAllViews();
        List<ProjectLibraryRepository.Project> projects = repository.listProjects(favoritesOnly, searchQuery);
        if (projects.isEmpty()) {
            list.addView(label("No saved projects yet.", 14, Color.rgb(100, 116, 139), false), matchWrapWithTop(12));
            return;
        }
        for (final ProjectLibraryRepository.Project project : projects) {
            LinearLayout card = new LinearLayout(activity);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(12), dp(9), dp(12), dp(9));
            card.setBackground(rounded(Color.rgb(248, 250, 252), Color.rgb(226, 232, 240)));
            LinearLayout.LayoutParams cardParams = matchWrapWithTop(7);
            list.addView(card, cardParams);
            TextView title = label(project.name, 15, Color.rgb(15, 23, 42), true);
            card.addView(title);
            card.addView(label(project.extensionName + (project.packageName.isEmpty() ? "" : " · " + project.packageName), 11,
                Color.rgb(71, 85, 105), false), matchWrapWithTop(3));
            card.addView(label("SHA-256 " + shortHash(project.checksum) + " · DEX " + (project.dexAvailable ? "available" : "missing")
                + " · metadata " + (project.metadataAvailable ? "available" : "missing"), 10, Color.rgb(100, 116, 139), false), matchWrapWithTop(4));
            LinearLayout actions = new LinearLayout(activity);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            card.addView(actions, matchWrapWithTop(4));
            addSmallAction(actions, "Open", new Runnable() { @Override public void run() { showProject(project); } });
            addSmallAction(actions, project.favorite ? "★" : "☆", new Runnable() { @Override public void run() {
                try { repository.setFavorite(project, !project.favorite); renderProjects(list); }
                catch (Exception e) { toast("Favorite update failed: " + e.getMessage()); }
            }});
            addSmallAction(actions, "Rename", new Runnable() { @Override public void run() { promptRename(project, list); } });
            addSmallAction(actions, "Local DEX copy", new Runnable() { @Override public void run() {
                try { File file = repository.exportDex(project); toast("Local DEX copy created at " + file.getAbsolutePath()); }
                catch (Exception e) { toast("Export failed: " + e.getMessage()); }
            }});
            addSmallAction(actions, "Delete", new Runnable() { @Override public void run() { confirmDelete(project, list); } });
        }
    }

    private void showProject(ProjectLibraryRepository.Project project) {
        try {
            JSONObject info = repository.readProject(project);
            JSONArray components = repository.readMetadata(project);
            JSONArray history = repository.readHistory(project);
            resetContent(project.name, "Offline project details");
            detail("Extension", info.optString("extensionName", "Unavailable"));
            detail("Package", info.optString("packageName", "Unavailable"));
            detail("Project ID", project.id);
            detail("DEX", project.dexAvailable ? "Present · " + new File(project.directory, "extension.dex").length() + " bytes" : "Missing");
            detail("SHA-256", project.checksum);
            detail("Created", formatDate(project.createdAt));
            detail("Updated", formatDate(project.updatedAt));
            detail("Last test", project.lastTestAt > 0 ? formatDate(project.lastTestAt) : "No recorded test");
            detail("Selected method snapshot", info.optString("methodName", "Unavailable"));
            detail("Arguments snapshot", info.optString("arguments", "Unavailable"));
            detail("Provider snapshot", info.optString("providerInfo", "Unavailable"));
            detail("Metadata", project.metadataAvailable ? components.length() + " component(s)" : "Unavailable");
            detail("Test history", history.length() + " recorded item(s)");
            detail("Notes", info.optString("userNotes", ""));
            for (int i = 0; i < components.length(); i++) {
                JSONObject component = components.optJSONObject(i);
                if (component == null) continue;
                content.addView(label(component.optString("type", "Component"), 14, Color.rgb(30, 64, 175), true), matchWrapWithTop(10));
                JSONArray methods = component.optJSONArray("methods");
                if (methods != null) for (int m = 0; m < methods.length(); m++) {
                    JSONObject method = methods.optJSONObject(m);
                    if (method == null) continue;
                    JSONArray params = method.optJSONArray("params");
                    StringBuilder signature = new StringBuilder(method.optString("name", "method")).append('(');
                    if (params != null) for (int p = 0; p < params.length(); p++) {
                        JSONObject param = params.optJSONObject(p);
                        if (p > 0) signature.append(", ");
                        signature.append(param == null ? "?" : param.optString("name", "parameter") + ":" + param.optString("type", "?"));
                    }
                    signature.append(')');
                    content.addView(label(signature.toString(), 11, Color.rgb(71, 85, 105), false), matchWrapWithTop(3));
                }
            }
            if (history.length() > 0) {
                content.addView(label("Recorded tests", 14, Color.rgb(15, 23, 42), true), matchWrapWithTop(12));
                for (int i = history.length() - 1; i >= 0; i--) {
                    JSONObject entry = history.optJSONObject(i);
                    if (entry == null) continue;
                    content.addView(label(formatDate(entry.optLong("timestamp", 0)) + " · " + entry.optString("status", "UNKNOWN")
                        + " · " + entry.optString("method", "") + " " + entry.optString("message", entry.optString("error", "")),
                        11, Color.rgb(71, 85, 105), false), matchWrapWithTop(4));
                }
            }
            content.addView(label("Offline mode is read-only. Stored DEX is not loaded or executed.", 11,
                Color.rgb(180, 83, 9), false), matchWrapWithTop(14));
        } catch (Exception e) { toast("Project could not be opened: " + e.getMessage()); }
    }

    private void showHistory() {
        resetContent("Test History", "Only recorded execution outcomes are shown.");
        List<ProjectLibraryRepository.Project> projects = repository.listProjects(false, "");
        int count = 0;
        for (ProjectLibraryRepository.Project project : projects) {
            try {
                JSONArray history = repository.readHistory(project);
                for (int i = history.length() - 1; i >= 0; i--) {
                    JSONObject entry = history.optJSONObject(i);
                    if (entry == null) continue;
                    count++;
                    content.addView(label(project.name + " · " + formatDate(entry.optLong("timestamp", 0)), 13,
                        Color.rgb(15, 23, 42), true), matchWrapWithTop(9));
                    content.addView(label(entry.optString("status", "UNKNOWN") + " · " + entry.optString("method", "")
                        + "(" + entry.optString("arguments", "") + ")\n"
                        + (entry.optString("providerInfo", "").isEmpty() ? "" : "Provider: " + entry.optString("providerInfo", "") + "\n")
                        + entry.optString("result", entry.optString("error", entry.optString("message", ""))), 11,
                        Color.rgb(71, 85, 105), false), matchWrapWithTop(3));
                }
            } catch (Exception ignored) {}
        }
        if (count == 0) content.addView(label("No execution history is stored yet.", 14, Color.rgb(100, 116, 139), false), matchWrapWithTop(10));
    }

    private void showDexFiles() {
        resetContent("Saved DEX", "DEX binaries stored in the local project library.");
        List<ProjectLibraryRepository.Project> projects = repository.listProjects(false, "");
        int count = 0;
        for (final ProjectLibraryRepository.Project project : projects) {
            if (!project.dexAvailable) continue;
            count++;
            File dex = new File(project.directory, "extension.dex");
            content.addView(label(project.name, 14, Color.rgb(15, 23, 42), true), matchWrapWithTop(8));
            content.addView(label(dex.getName() + " · " + dex.length() + " bytes\nSHA-256 " + project.checksum
                + "\nMetadata " + (project.metadataAvailable ? "available" : "missing"), 11,
                Color.rgb(71, 85, 105), false), matchWrapWithTop(3));
        }
        if (count == 0) content.addView(label("No saved DEX files.", 14, Color.rgb(100, 116, 139), false), matchWrapWithTop(10));
    }

    private void showImportInfo() {
        resetContent("Import Project", "Import support is not enabled in this first storage foundation.");
        content.addView(label("The current extension does not own an Activity Result API for document pickers. Import/export archive support needs an explicit host-safe integration. Existing projects remain available offline.",
            13, Color.rgb(71, 85, 105), false), matchWrapWithTop(10));
    }

    private void showSettings() {
        resetContent("Library Settings", "Storage and integrity details.");
        detail("Storage", repository.getRootDirectory().getAbsolutePath());
        detail("Scope", "App-private files; removed when the host app is uninstalled or its data is cleared.");
        detail("Integrity", "SHA-256 identifies the stored DEX; it is not a safety guarantee.");
    }

    private void promptRename(final ProjectLibraryRepository.Project project, final LinearLayout list) {
        final EditText input = new EditText(activity);
        input.setText(project.name);
        new AlertDialog.Builder(activity).setTitle("Rename Project").setView(input)
            .setNegativeButton("Cancel", null).setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface d, int which) {
                    try { repository.rename(project, input.getText().toString()); renderProjects(list); }
                    catch (Exception e) { toast("Rename failed: " + e.getMessage()); }
                }
            }).show();
    }

    private void confirmDelete(final ProjectLibraryRepository.Project project, final LinearLayout list) {
        new AlertDialog.Builder(activity).setTitle("Delete Project?").setMessage("Delete " + project.name + " and its stored files?")
            .setNegativeButton("Cancel", null).setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface d, int which) {
                    try { repository.delete(project); renderProjects(list); }
                    catch (Exception e) { toast("Delete failed: " + e.getMessage()); }
                }
            }).show();
    }

    private void addSmallAction(LinearLayout row, String text, final Runnable action) {
        TextView button = label(text, 10, Color.rgb(29, 78, 216), true);
        button.setPadding(dp(5), dp(6), dp(7), dp(4));
        row.addView(button, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { action.run(); } });
    }

    private void detail(String title, String value) {
        content.addView(label(title, 11, Color.rgb(100, 116, 139), true), matchWrapWithTop(8));
        content.addView(label(value == null || value.isEmpty() ? "Unavailable" : value, 13, Color.rgb(15, 23, 42), false), matchWrapWithTop(2));
    }

    private void resetContent(String title, String subtitle) {
        content.removeAllViews();
        content.addView(label(title, 21, Color.rgb(15, 23, 42), true), matchWrap());
        content.addView(label(subtitle, 12, Color.rgb(100, 116, 139), false), matchWrapWithTop(4));
    }

    private Button action(String title, int color) {
        Button button = new Button(activity);
        button.setText(title);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setBackground(rounded(color, color));
        return button;
    }

    private TextView label(String text, int size, int color, boolean bold) {
        TextView view = new TextView(activity);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(null, Typeface.BOLD);
        return view;
    }

    private GradientDrawable rounded(int color, int stroke) {
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(10));
        bg.setStroke(dp(1), stroke);
        return bg;
    }

    private LinearLayout.LayoutParams matchWrap() { return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); }
    private LinearLayout.LayoutParams matchWrapWithTop(int margin) {
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(margin);
        return params;
    }
    private int dp(int value) { return Math.round(value * activity.getResources().getDisplayMetrics().density); }
    private String shortHash(String hash) { return hash == null || hash.length() < 12 ? (hash == null ? "unavailable" : hash) : hash.substring(0, 12) + "…"; }
    private String formatDate(long time) { return time <= 0 ? "Not recorded" : DateFormat.getDateTimeInstance().format(new Date(time)); }
    private void toast(String text) { Toast.makeText(activity, text, Toast.LENGTH_LONG).show(); }
}
