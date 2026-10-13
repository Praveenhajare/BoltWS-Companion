package com.supertech.BoltWS;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** App-private, atomic storage for saved BoltWS test projects. */
final class ProjectLibraryRepository {
    private static final String ROOT_NAME = "BoltWSProjectLibrary";
    private final File root;

    static final class Project {
        final String id;
        final String name;
        final String extensionName;
        final String packageName;
        final String checksum;
        final long createdAt;
        final long updatedAt;
        final long lastTestAt;
        final boolean favorite;
        final String methodName;
        final String arguments;
        final File directory;
        final boolean dexAvailable;
        final boolean metadataAvailable;

        Project(JSONObject json, File directory) {
            this.id = json.optString("id", directory.getName());
            this.name = json.optString("name", this.id);
            this.extensionName = json.optString("extensionName", "");
            this.packageName = json.optString("packageName", "");
            this.checksum = json.optString("sha256", "");
            this.createdAt = json.optLong("createdAt", 0L);
            this.updatedAt = json.optLong("updatedAt", 0L);
            this.lastTestAt = json.optLong("lastTestAt", 0L);
            this.favorite = json.optBoolean("favorite", false);
            this.methodName = json.optString("methodName", "");
            this.arguments = json.optString("arguments", "");
            this.directory = directory;
            this.dexAvailable = new File(directory, "extension.dex").isFile();
            this.metadataAvailable = new File(directory, "components.json").isFile();
        }
    }

    ProjectLibraryRepository(Context context) throws IOException {
        root = new File(context.getApplicationContext().getFilesDir(), ROOT_NAME);
        if (!root.isDirectory() && !root.mkdirs()) throw new IOException("Could not create project library directory.");
    }

    synchronized Project saveProject(String name, String extensionName, String packageName,
                                     byte[] dex, String componentsJson, String methodName,
                                     String arguments, String providerInfo, String userNotes) throws IOException, JSONException {
        if (dex == null || dex.length < 8 || !hasDexMagic(dex)) throw new IOException("The received binary is not a valid DEX header.");
        String id = UUID.randomUUID().toString();
        File directory = new File(root, "project_" + id);
        if (!directory.mkdir()) throw new IOException("Could not create project directory.");
        long now = System.currentTimeMillis();
        try {
            writeAtomic(new File(directory, "extension.dex"), dex);
            if (componentsJson != null && !componentsJson.trim().isEmpty()) {
                JSONObject metadata = new JSONObject(componentsJson);
                writeAtomic(new File(directory, "components.json"), metadata.toString(2).getBytes("UTF-8"));
            }
            JSONObject project = new JSONObject();
            project.put("schemaVersion", 1);
            project.put("id", id);
            project.put("name", clean(name, "Untitled project"));
            project.put("extensionName", clean(extensionName, ""));
            project.put("packageName", clean(packageName, ""));
            project.put("dexFile", "extension.dex");
            project.put("metadataFile", componentsJson != null && !componentsJson.trim().isEmpty() ? "components.json" : "");
            project.put("historyFile", "test-history.json");
            project.put("sha256", sha256(dex));
            project.put("createdAt", now);
            project.put("updatedAt", now);
            project.put("lastTestAt", JSONObject.NULL);
            project.put("favorite", false);
            project.put("methodName", clean(methodName, ""));
            project.put("arguments", clean(arguments, ""));
            project.put("providerInfo", clean(providerInfo, ""));
            project.put("userNotes", clean(userNotes, ""));
            project.put("metadataAvailable", componentsJson != null && !componentsJson.trim().isEmpty());
            JSONArray history = new JSONArray();
            writeJsonAtomic(new File(directory, "project.json"), project);
            writeJsonAtomic(new File(directory, "test-history.json"), history);
            return new Project(project, directory);
        } catch (Exception e) {
            deleteRecursively(directory);
            if (e instanceof IOException) throw (IOException) e;
            if (e instanceof JSONException) throw (JSONException) e;
            throw new IOException("Could not save project: " + e.getMessage(), e);
        }
    }

    synchronized List<Project> listProjects(boolean favoritesOnly, String query) {
        List<Project> projects = new ArrayList<Project>();
        File[] children = root.listFiles();
        if (children == null) return projects;
        String needle = query == null ? "" : query.trim().toLowerCase(java.util.Locale.ROOT);
        for (File child : children) {
            if (!child.isDirectory()) continue;
            try {
                JSONObject json = readJson(new File(child, "project.json"));
                Project project = new Project(json, child);
                if (favoritesOnly && !project.favorite) continue;
                String haystack = (project.name + " " + project.extensionName + " " + project.packageName).toLowerCase(java.util.Locale.ROOT);
                if (!needle.isEmpty() && !haystack.contains(needle)) continue;
                projects.add(project);
            } catch (Exception ignored) {
                // Keep invalid records on disk; skip them rather than risking unrelated data loss.
            }
        }
        Collections.sort(projects, new Comparator<Project>() {
            @Override public int compare(Project a, Project b) {
                if (a.updatedAt == b.updatedAt) return 0;
                return a.updatedAt > b.updatedAt ? -1 : 1;
            }
        });
        return projects;
    }

    synchronized Project findByDexSha256(String checksum) {
        if (checksum == null || checksum.isEmpty()) return null;
        for (Project project : listProjects(false, "")) {
            if (checksum.equalsIgnoreCase(project.checksum)) return project;
        }
        return null;
    }

    synchronized JSONObject readProject(Project project) throws IOException, JSONException {
        requireProject(project);
        return readJson(new File(project.directory, "project.json"));
    }

    synchronized JSONArray readMetadata(Project project) throws IOException, JSONException {
        requireProject(project);
        File file = new File(project.directory, "components.json");
        if (!file.isFile()) return new JSONArray();
        JSONObject envelope = readJson(file);
        JSONArray components = envelope.optJSONArray("components");
        return components == null ? new JSONArray() : components;
    }

    synchronized JSONArray readHistory(Project project) throws IOException, JSONException {
        requireProject(project);
        File file = new File(project.directory, "test-history.json");
        if (!file.isFile()) return new JSONArray();
        byte[] bytes = readAll(file);
        return new JSONArray(new String(bytes, "UTF-8"));
    }

    synchronized void rename(Project project, String name) throws IOException, JSONException {
        JSONObject json = readProject(project);
        json.put("name", clean(name, project.name));
        json.put("updatedAt", System.currentTimeMillis());
        writeJsonAtomic(new File(project.directory, "project.json"), json);
    }

    synchronized void setFavorite(Project project, boolean favorite) throws IOException, JSONException {
        JSONObject json = readProject(project);
        json.put("favorite", favorite);
        json.put("updatedAt", System.currentTimeMillis());
        writeJsonAtomic(new File(project.directory, "project.json"), json);
    }

    synchronized void appendTest(Project project, String method, String arguments, String status,
                                 String providerInfo, String result, String error) throws IOException, JSONException {
        JSONArray history = readHistory(project);
        JSONObject entry = new JSONObject();
        long now = System.currentTimeMillis();
        entry.put("id", UUID.randomUUID().toString());
        entry.put("timestamp", now);
        entry.put("sha256", project.checksum);
        entry.put("method", clean(method, ""));
        entry.put("arguments", clean(arguments, ""));
        entry.put("providerInfo", clean(providerInfo, ""));
        entry.put("status", clean(status, "UNKNOWN"));
        if (result != null) entry.put("result", result);
        if (error != null) entry.put("error", error);
        history.put(entry);
        writeJsonAtomic(new File(project.directory, "test-history.json"), history);
        JSONObject json = readProject(project);
        json.put("lastTestAt", now);
        json.put("updatedAt", now);
        writeJsonAtomic(new File(project.directory, "project.json"), json);
    }

    synchronized void delete(Project project) throws IOException {
        requireProject(project);
        if (!deleteRecursively(project.directory)) throw new IOException("Could not delete the saved project.");
    }

    synchronized File exportDex(Project project) throws IOException {
        requireProject(project);
        File dex = new File(project.directory, "extension.dex");
        if (!dex.isFile()) throw new IOException("Project DEX is missing.");
        File exports = new File(root, "exports");
        if (!exports.isDirectory() && !exports.mkdirs()) throw new IOException("Could not create export directory.");
        File out = new File(exports, safeFilename(project.name) + "-" + project.checksum.substring(0, 8) + ".dex");
        copyAtomic(dex, out);
        return out;
    }

    File getRootDirectory() { return root; }

    private void requireProject(Project project) throws IOException {
        if (project == null || project.directory == null || !project.directory.getParentFile().equals(root)) {
            throw new IOException("Project does not belong to this library.");
        }
    }

    private JSONObject readJson(File file) throws IOException, JSONException {
        if (!file.isFile()) throw new IOException("Project record is missing: " + file.getName());
        return new JSONObject(new String(readAll(file), "UTF-8"));
    }

    private void writeJsonAtomic(File file, JSONObject json) throws IOException {
        try { writeAtomic(file, json.toString(2).getBytes("UTF-8")); }
        catch (JSONException e) { throw new IOException("Could not serialize project record.", e); }
    }

    private void writeJsonAtomic(File file, JSONArray json) throws IOException {
        try { writeAtomic(file, json.toString(2).getBytes("UTF-8")); }
        catch (JSONException e) { throw new IOException("Could not serialize project history.", e); }
    }

    private void writeAtomic(File target, byte[] data) throws IOException {
        File temp = new File(target.getParentFile(), target.getName() + ".tmp");
        File previous = new File(target.getParentFile(), target.getName() + ".previous");
        if (!target.exists() && previous.exists()) previous.renameTo(target);
        FileOutputStream stream = new FileOutputStream(temp);
        try { stream.write(data); stream.flush(); stream.getFD().sync(); } finally { stream.close(); }
        if (previous.exists() && !previous.delete()) { temp.delete(); throw new IOException("Could not clear recovery record for " + target.getName()); }
        boolean movedOld = target.exists() && target.renameTo(previous);
        if (target.exists() && !movedOld) { temp.delete(); throw new IOException("Could not stage existing " + target.getName()); }
        if (!temp.renameTo(target)) {
            if (movedOld) previous.renameTo(target);
            temp.delete();
            throw new IOException("Could not commit " + target.getName());
        }
        if (previous.exists()) previous.delete();
    }

    private void copyAtomic(File source, File target) throws IOException { writeAtomic(target, readAll(source)); }

    private byte[] readAll(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        try { int count; while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count); }
        finally { in.close(); }
        return out.toByteArray();
    }

    private boolean hasDexMagic(byte[] bytes) {
        return bytes.length >= 8 && bytes[0] == 0x64 && bytes[1] == 0x65 && bytes[2] == 0x78 && bytes[3] == 0x0A;
    }

    private String sha256(byte[] bytes) throws IOException {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder result = new StringBuilder();
            for (byte value : hash) result.append(String.format(java.util.Locale.ROOT, "%02x", value & 0xff));
            return result.toString();
        } catch (Exception e) { throw new IOException("SHA-256 is unavailable.", e); }
    }

    private String clean(String value, String fallback) { return value == null ? fallback : value.trim(); }
    private String safeFilename(String name) { return name.replaceAll("[^A-Za-z0-9._-]", "_"); }

    private boolean deleteRecursively(File file) {
        if (file == null || !file.exists()) return true;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) for (File child : children) if (!deleteRecursively(child)) return false;
        }
        return file.delete();
    }
}
