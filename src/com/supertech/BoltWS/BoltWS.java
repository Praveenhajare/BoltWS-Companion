package com.supertech.BoltWS;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.Form;
import com.google.appinventor.components.runtime.util.YailDictionary;
import com.google.appinventor.components.runtime.util.YailList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONObject;
import org.json.JSONArray;
import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

@DesignerComponent(
    version = 208,
    description = "Universal Bolt Live Dev Panel: Fully Automatic Universal Chameleon Engine for Random Extensions",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "icon.png"
)
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.ACCESS_NETWORK_STATE")
public class BoltWS extends AndroidNonvisibleComponent {

    private LinearLayout panelBody = null;
    private android.app.AlertDialog tuningDialog = null;
    private LinearLayout openTuningTile = null;

    private final Activity activity;
    private final Handler mainHandler;
    private final Form formInstance;

    private static final int DEFAULT_WS_PORT = 9000;
    private static final String PREF_NAME = "BoltMethodPresetsV15";
    private static final String PREF_LANG_KEY = "BoltSelectedLangCode";
    private static final String LEGACY_PREF_LANG_KEY = "BoltSelectedLangIndex";

    // १८ आधुनिक व व्हायब्रंट रंगांची विस्तृत कलर पॅलेट
    private static final String[] EXTENDED_COLOR_PALETTE = new String[]{
        "#FFFFFF", "#000000", "#E0E5EC", "#1E293B", "#64748B",
        "#EF4444", "#F97316", "#F59E0B", "#EAB308",
        "#10B981", "#06B6D4", "#0EA5E9", "#3B82F6",
        "#6366F1", "#8B5CF6", "#A855F7", "#D946EF",
        "#EC4899", "#F43F5E"
    };

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private Thread wsWorkerThread;
    private volatile boolean isConnected = false;
    private String lastConnectedHost = "";
    private int lastConnectedPort = DEFAULT_WS_PORT;
    private boolean autoReconnectEnabled = true;
    private volatile String lastDiscoveryStatus = "NOT TESTED";
    private volatile String lastDiscoveryMessage = "";

    private byte[] lastBinaryData = null;
    private String lastDexChecksum = "";
    private String lastMetadataDexChecksum = "";
    private String lastMetadataEnvelope = "";
    private String lastHotReloadDexChecksum = "";
    private long lastInboundTextTimeMillis = 0L;
    private String blockPropertyMetadataDexChecksum = "";
    private File lastSavedDexFile = null;
    private ProjectLibraryRepository projectLibraryRepository = null;
    private volatile String lastDexVerificationStatus = "NOT TESTED";
    private volatile String lastDexVerificationMessage = "";
    private DexClassLoader currentClassLoader = null;
    private Class<?> currentExtensionClass = null;
    private Object currentExtensionInstance = null;
    private String currentClassName = "";
    private volatile String lastConstructorSignature = "";
    private volatile String lastExtensionLoadStatus = "NOT TESTED";
    private volatile String lastExtensionLoadMessage = "";
    private volatile String lastErrorMessage = "";
    private volatile String currentErrorMessage = "";
    private volatile String previousErrorMessage = "";
    private volatile String lastExecutionStatus = "NOT TESTED";
    private volatile String lastExecutionMethod = "";
    private volatile String lastExecutionMessage = "";
    private volatile String lastExecutionResult = "";

    // Header Card Views
    private LinearLayout autoHeaderCard = null;
    private TextView cardMethodNameView = null;
    private TextView cardTitleView = null;
    private TextView cardIpView = null;
    private TextView cardStatusView = null;
    private Button cardFindPcButton = null;

    private LinearLayout panelContainer = null;
    private ViewGroup attachedPanelHost = null;
    private Spinner methodSpinner = null;
    private KodularBlockAdapter methodAdapter = null;
    private Spinner targetViewSpinner = null;
    private Spinner languageSpinner = null;
    private int currentLanguageIndex = 0;
    private String currentLanguageCode = "en";

    private final List<View> availableTargetViews = new ArrayList<View>();
    private final List<String> availableTargetNames = new ArrayList<String>();
    private final Map<View, TargetViewSnapshot> targetViewSnapshots = new IdentityHashMap<View, TargetViewSnapshot>();
    private boolean userManuallySelectedTarget = false;

    private static final class TargetViewSnapshot {
        final List<View> ancestry;
        final View listContainer;
        final View listItemRoot;
        final int itemPosition;
        final boolean hasStableItemId;
        final long stableItemId;

        TargetViewSnapshot(List<View> ancestry, View listContainer, View listItemRoot,
                           int itemPosition, boolean hasStableItemId, long stableItemId) {
            this.ancestry = ancestry;
            this.listContainer = listContainer;
            this.listItemRoot = listItemRoot;
            this.itemPosition = itemPosition;
            this.hasStableItemId = hasStableItemId;
            this.stableItemId = stableItemId;
        }
    }

    private TextView paramInfoLabel = null;
    private EditText argsEditText = null;
    private final Map<Integer, ProviderConfiguration> selectedProviderConfigurations = new HashMap<Integer, ProviderConfiguration>();
    private final Map<String, List<String>> providerArgumentValueCache = new HashMap<String, List<String>>();
    private final List<ProviderSelectionSlot> providerSelectionSlots = new ArrayList<ProviderSelectionSlot>();
    private String providerSelectionMainMethod = "";
    private Runnable pendingProviderPreviewRunnable = null;
    private String autoRenderPendingMethodIdentity = "";
    private Button copyArgsBtn = null;
    private Button runButton = null;
    private Button resetButton = null;
    private Button toggleLogsButton = null;
    private Button clearLogsButton = null;
    private Button minimizeButton = null;
    private Button rescanTargetsButton = null;
    private TextView consoleTitle = null;
    private TextView consoleTextView = null;
    private ScrollView consoleScrollView = null;
    private LinearLayout liveConsoleLayout = null;
    private TextView panelTitleView = null;
    private String lastCardHostIp = "";
    private String lastCardStatusMessage = "Extension Ready";
    private String lastCardMethodName = "Bolt Companion Ready";
    private AndroidViewComponent defaultTargetComponent = null;

    private boolean isConsoleVisible = true;
    private boolean isPanelMinimized = false;
    private String userSelectedMethod = "";
    private final List<Method> currentLoadedMethodEntries = new ArrayList<Method>();
    private final Map<String, List<String>> blockParameterNamesByMethod = new HashMap<String, List<String>>();
    private final Map<String, List<BlockArgumentMetadata>> blockArgumentMetadataByMethod = new HashMap<String, List<BlockArgumentMetadata>>();
    private final List<BlockPropertyMetadata> blockPropertyMetadata = new ArrayList<BlockPropertyMetadata>();
    private final Map<String, String> selectedGetterProperties = new HashMap<String, String>();
    private String lastMethodResolutionError = "";
    private final Map<String, String> argumentsCache = new HashMap<String, String>();
    private final Map<String, Object> customObjectResults = new HashMap<String, Object>();

    private static final class BlockPropertyMetadata {
        final String name;
        final String type;
        final String access;

        BlockPropertyMetadata(String name, String type, String access) {
            this.name = name;
            this.type = type;
            this.access = access;
        }
    }

    private final Map<View, Drawable.ConstantState> pristineBackgroundCache = new java.util.IdentityHashMap<View, Drawable.ConstantState>();
    private final Map<View, Boolean> pristineBackgroundCaptured = new java.util.IdentityHashMap<View, Boolean>();
    private final AtomicLong globalRenderGeneration = new AtomicLong(0L);
    private Runnable pendingRenderRunnable = null;
    private static final long RENDER_FLUSH_DELAY_MS = 60L;
    private final List<View> dynamicallyAddedViews = new ArrayList<View>();

    public BoltWS(ComponentContainer container) {
        super(container.$form());
        this.formInstance = container.$form();
        this.activity = (Activity) container.$context();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.currentLanguageCode = loadSavedLanguageCode();
        this.currentLanguageIndex = Localization.getLanguageIndex(currentLanguageCode);
    }

    private String loadSavedLanguageCode() {
        try {
            SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            Object savedLanguage = prefs.getAll().get(PREF_LANG_KEY);
            if (savedLanguage instanceof String) {
                String code = (String) savedLanguage;
                return Localization.getLanguageCode(Localization.getLanguageIndex(code));
            }
            savedLanguage = prefs.getAll().get(LEGACY_PREF_LANG_KEY);
            if (savedLanguage instanceof Integer) {
                int legacyIndex = ((Integer) savedLanguage).intValue();
                String migratedCode = Localization.getLanguageCode(legacyIndex);
                prefs.edit().putString(PREF_LANG_KEY, migratedCode).apply();
                return migratedCode;
            }
        } catch (Exception ignored) {
            // English remains the safe default when preferences are unavailable.
        }
        return "en";
    }

    private int dipToPx(int dp) {
        if (activity == null) return dp;
        float density = activity.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private int safePxToDip(int px) {
        if (activity == null) return px;
        float density = activity.getResources().getDisplayMetrics().density;
        return (density > 0) ? Math.round(px / density) : px;
    }

    private File getCompatibleCodeCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                ? activity.getCodeCacheDir()
                : activity.getCacheDir();
    }

    private void updateTargetNativeSize(int dpH, int dpW) {
        try {
            View v = getSelectedTargetNativeView();
            if (v != null && v.getLayoutParams() != null) {
                if (dpH > 0) v.getLayoutParams().height = dipToPx(dpH);
                if (dpW > 0) v.getLayoutParams().width = dipToPx(dpW);
                v.requestLayout();
            }
        } catch (Exception ignored) {}
    }

    private List<String> parseArgumentTokens(String raw) {
        if (raw == null || raw.trim().isEmpty()) return new ArrayList<String>();
        return splitDelimitedValues(raw, false);
    }

    private List<String> splitDelimitedValues(String raw, boolean allowSemicolon) {
        List<String> values = new ArrayList<String>();
        if (raw == null || raw.isEmpty()) return values;

        StringBuilder token = new StringBuilder();
        List<Character> closing = new ArrayList<Character>();
        char quote = 0;
        boolean escaped = false;

        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (quote != 0) {
                token.append(c);
                if (escaped) {
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == quote) {
                    quote = 0;
                }
                continue;
            }

            if (c == '"') {
                quote = c;
                token.append(c);
            } else if (c == '[' || c == '{' || c == '(') {
                closing.add(c == '[' ? ']' : (c == '{' ? '}' : ')'));
                token.append(c);
            } else if (c == ']' || c == '}' || c == ')') {
                if (closing.isEmpty() || closing.remove(closing.size() - 1).charValue() != c) {
                    throw new IllegalArgumentException("Unbalanced brackets in argument input.");
                }
                token.append(c);
            } else if (closing.isEmpty() && (c == ',' || (allowSemicolon && c == ';'))) {
                values.add(token.toString().trim());
                token.setLength(0);
            } else {
                token.append(c);
            }
        }

        if (quote != 0 || !closing.isEmpty()) {
            throw new IllegalArgumentException("Unclosed quote or bracket in argument input.");
        }
        values.add(token.toString().trim());
        return values;
    }

    private String formatArgumentToken(String value) {
        if (value == null) return "";
        if (value.isEmpty()) return "\"\"";
        String trimmed = value.trim();
        boolean structured = (trimmed.startsWith("[") && trimmed.endsWith("]"))
                || (trimmed.startsWith("{") && trimmed.endsWith("}"));
        if (structured || (trimmed.indexOf(',') < 0 && trimmed.indexOf('"') < 0 && trimmed.indexOf('\\') < 0
                && trimmed.length() == value.length())) {
            return value;
        }

        StringBuilder quoted = new StringBuilder(value.length() + 2);
        quoted.append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"': quoted.append("\\\""); break;
                case '\\': quoted.append("\\\\"); break;
                case '\b': quoted.append("\\b"); break;
                case '\f': quoted.append("\\f"); break;
                case '\n': quoted.append("\\n"); break;
                case '\r': quoted.append("\\r"); break;
                case '\t': quoted.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        String hex = Integer.toHexString(c);
                        quoted.append("\\u");
                        for (int pad = hex.length(); pad < 4; pad++) quoted.append('0');
                        quoted.append(hex);
                    } else {
                        quoted.append(c);
                    }
            }
        }
        quoted.append('"');
        return quoted.toString();
    }

    private String decodeQuotedArgument(String raw) {
        String value = raw != null ? raw.trim() : "";
        if (value.startsWith("\"") != value.endsWith("\"")) {
            throw new IllegalArgumentException("Quoted argument is not closed.");
        }
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            try {
                JSONArray oneValue = new JSONArray("[" + value + "]");
                if (oneValue.length() != 1 || !(oneValue.get(0) instanceof String)) {
                    throw new IllegalArgumentException("Expected a quoted string literal.");
                }
                return oneValue.getString(0);
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid quoted string literal.", e);
            }
        }
        return value;
    }

    private Object normalizeJsonValue(Object value) throws Exception {
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            List<Object> result = new ArrayList<Object>();
            for (int i = 0; i < array.length(); i++) result.add(normalizeJsonValue(array.get(i)));
            return result;
        }
        if (value instanceof JSONObject) {
            JSONObject object = (JSONObject) value;
            Map<String, Object> result = new HashMap<String, Object>();
            java.util.Iterator<String> keys = object.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                result.put(key, normalizeJsonValue(object.get(key)));
            }
            return result;
        }
        return value;
    }

    private Object parseListItem(String raw) throws Exception {
        String item = raw.trim();
        if (item.startsWith("\"") && item.endsWith("\"")) return decodeQuotedArgument(item);
        if (item.startsWith("[") && item.endsWith("]")) {
            try {
                return normalizeJsonValue(new JSONArray(item));
            } catch (Exception notJsonArray) {
                List<String> parts = splitDelimitedValues(item.substring(1, item.length() - 1), true);
                List<Object> nested = new ArrayList<Object>();
                for (String part : parts) nested.add(parseListItem(part));
                return nested;
            }
        }
        if (item.startsWith("{") && item.endsWith("}")) {
            return normalizeJsonValue(new JSONObject(item));
        }
        if (item.startsWith("#")) {
            try { return Color.parseColor(item); } catch (Exception ignored) {}
        }
        return item;
    }

    private Object parseListItemSafely(String raw) {
        try {
            return parseListItem(raw);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid nested list value: " + raw, e);
        }
    }

    private List<String> getAvailableGetters() {
        ensureDiscoveredGetterMetadata();
        List<String> names = new ArrayList<String>();
        for (BlockPropertyMetadata property : blockPropertyMetadata) {
            if (findPropertyAccessor(property, true) != null) names.add(property.name);
        }
        return names;
    }


    private boolean isVerifiedGetterCandidate(Method method) {
        if (method == null) return false;
        if (method.isBridge() || method.isSynthetic()) return false;
        if (java.lang.reflect.Modifier.isStatic(method.getModifiers())) return false;
        if (method.getParameterTypes().length != 0) return false;
        if (method.getReturnType() == void.class) return false;
        if (method.getDeclaringClass() == Object.class) return false;
        String name = method.getName();

        // Niyam 1: JavaBean-style (getXxx / isXxx)
        if ((name.startsWith("get") && name.length() > 3)
                || (name.startsWith("is") && name.length() > 2)) return true;

        // Niyam 2: @SimpleProperty annotation (DEX madhe urle asel tar)
        try {
            for (java.lang.annotation.Annotation a : method.getAnnotations()) {
                if (a.annotationType().getName().endsWith(".SimpleProperty")) return true;
            }
        } catch (Throwable ignored) {}

        // Niyam 3: Setter-mirror vagla (same name, 1-arg, void)
        try {
            for (Method sibling : method.getDeclaringClass().getMethods()) {
                if (sibling.getName().equals(name)
                        && sibling.getParameterTypes().length == 1
                        && sibling.getReturnType() == void.class
                        && !sibling.isBridge() && !sibling.isSynthetic()) {
                    return false;
                }
            }
        } catch (Throwable ignored) {}

        // Niyam 4: Universal fallback — zero-arg, non-void, non-Object, non-setter-mirror
        // method getter mhanun accept kara. He Kodular @SimpleProperty getters sathi
        // aavashyak ahe jyanche annotation DEX madhe gaib hote (uda. BottomToTop).
        return true;
    }

    private void ensureDiscoveredGetterMetadata() {
        if (currentExtensionClass == null || currentExtensionInstance == null
                || !currentExtensionClass.isInstance(currentExtensionInstance)
                || lastDexChecksum == null || lastDexChecksum.isEmpty()) return;

        if (lastDexChecksum.equals(blockPropertyMetadataDexChecksum)) {
            for (BlockPropertyMetadata property : blockPropertyMetadata) {
                if (findPropertyAccessor(property, true) != null) return;
            }
        } else {
            blockPropertyMetadata.clear();
            selectedGetterProperties.clear();
        }

        for (Method candidate : currentExtensionClass.getMethods()) {
            if (!isVerifiedGetterCandidate(candidate)) continue;

            String name = candidate.getName();

            String type = inferGetterBlockType(candidate.getReturnType());
            if (type == null) {
                logToConsole("[GETTER-SKIP] " + candidate.getName()
                        + " returnType=" + candidate.getReturnType().getName()
                        + " is unsupported");
                continue;
            }

            logToConsole("[GETTER-CANDIDATE] " + candidate.getName()
                    + " returnType=" + candidate.getReturnType().getName()
                    + " blockType=" + type);

            boolean duplicate = false;
            for (BlockPropertyMetadata existing : blockPropertyMetadata) {
                if (existing.name.equals(name)) {
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate) {
                blockPropertyMetadata.add(
                    new BlockPropertyMetadata(name, type, "read-only"));
            }
        }

        blockPropertyMetadataDexChecksum = lastDexChecksum;
        logToConsole("[GETTER-DISCOVERY] " + blockPropertyMetadata.size()
                + " candidate getter(s) found for " + currentClassName);
    }

    private String getPropertySelectionKey(Method method, int argumentIndex) {
        return formatMethodIdentity(method) + "#" + argumentIndex;
    }

    private List<BlockPropertyMetadata> getCompatibleGetterProperties(Class<?> parameterType) {
        ensureDiscoveredGetterMetadata();
        List<BlockPropertyMetadata> compatible = new ArrayList<BlockPropertyMetadata>();
        if (parameterType == null || !lastDexChecksum.equals(blockPropertyMetadataDexChecksum)) return compatible;
        for (BlockPropertyMetadata property : blockPropertyMetadata) {

            Method accessor = findPropertyAccessor(property, true);

            if (accessor == null) {
                logToConsole("[GETTER-REJECT] property=" + property.name + " accessor=NULL");
                continue;
            }

            Class<?> valueType = accessor.getReturnType();

            boolean propertyToParam =
                    isBlockParameterTypeCompatible(property.type, parameterType);

            boolean propertyToGetter =
                    isBlockParameterTypeCompatible(property.type, valueType);

            logToConsole(
                "[GETTER-CHECK] property=" + property.name
                + " blockType=" + property.type
                + " paramType=" + parameterType.getSimpleName()
                + " getterType=" + valueType.getSimpleName()
                + " propertyToParam=" + propertyToParam
                + " propertyToGetter=" + propertyToGetter);

            boolean javaTypeStrict =
                    isJavaTypeStrictlyCompatible(valueType, parameterType);

            logToConsole(
                "[GETTER-CHECK] property=" + property.name
                + " blockType=" + property.type
                + " paramType=" + parameterType.getSimpleName()
                + " getterType=" + valueType.getSimpleName()
                + " propertyToParam=" + propertyToParam
                + " propertyToGetter=" + propertyToGetter
                + " javaTypeStrict=" + javaTypeStrict);

            if (propertyToParam && propertyToGetter && javaTypeStrict) {
                compatible.add(property);

                logToConsole(
                    "[GETTER-COMPATIBLE] property=" + property.name);
            }
        }
        return compatible;
    }

    private Method findPropertyAccessor(BlockPropertyMetadata property, boolean getter) {
        if (property == null || currentExtensionClass == null || currentExtensionInstance == null
                || !currentExtensionClass.isInstance(currentExtensionInstance)
                || !lastDexChecksum.equals(blockPropertyMetadataDexChecksum)
                || findBlockProperty(property.name) != property) return null;
        boolean hasAccess = getter
                ? ("read-only".equals(property.access) || "read-write".equals(property.access))
                : ("read-write".equals(property.access) || "write-only".equals(property.access));
        if (!hasAccess) return null;

        List<Method> candidates = new ArrayList<Method>();
        for (Method candidate : currentExtensionClass.getMethods()) {
            if (candidate.getDeclaringClass() == Object.class
                    || candidate.isBridge() || candidate.isSynthetic()) continue;

            String candidateName = candidate.getName();
            String propertyName = property.name;

            boolean nameMatch =
                    candidateName.equals(propertyName)
                    || candidateName.equalsIgnoreCase(propertyName)
                    || candidateName.equals("get" + propertyName)
                    || candidateName.equals("is" + propertyName)
                    || propertyName.equals("get" + candidateName)
                    || propertyName.equals("is" + candidateName);

            if (!nameMatch) continue;
            Class<?>[] parameterTypes = candidate.getParameterTypes();
            if (getter) {
                if (parameterTypes.length == 0 && candidate.getReturnType() != void.class
                        && isBlockParameterTypeCompatible(property.type, candidate.getReturnType())) {
                    candidates.add(candidate);
                }
            } else if (parameterTypes.length == 1 && candidate.getReturnType() == void.class
                    && isBlockParameterTypeCompatible(property.type, parameterTypes[0])) {
                candidates.add(candidate);
            }
        }
        return candidates.size() == 1 ? candidates.get(0) : null;
    }

    private String inferGetterBlockType(Class<?> type) {
        if (type == String.class) return "text";
        if (type == boolean.class || type == Boolean.class) return "boolean";
        if (type == byte.class || type == Byte.class || type == short.class || type == Short.class
                || type == int.class || type == Integer.class || type == long.class || type == Long.class
                || type == float.class || type == Float.class || type == double.class || type == Double.class) {
            return "number";
        }
        if (Component.class.isAssignableFrom(type) || AndroidViewComponent.class.isAssignableFrom(type)) {
            return "component";
        }
        return null;
    }

    private BlockPropertyMetadata findBlockProperty(String propertyName) {
        if (propertyName == null || !lastDexChecksum.equals(blockPropertyMetadataDexChecksum)) return null;
        BlockPropertyMetadata match = null;
        for (BlockPropertyMetadata property : blockPropertyMetadata) {
            if (property.name.equals(propertyName)) {
                if (match != null) return null;
                match = property;
            }
        }
        return match;
    }

    private boolean isCurrentExtensionMethod(Method method) {
        return method != null && currentExtensionClass != null && currentExtensionInstance != null
                && method.getDeclaringClass() == currentExtensionClass
                && currentExtensionClass.isInstance(currentExtensionInstance);
    }

    private BlockPropertyMetadata findVerifiedPropertySetter(Method method) {
        if (!isCurrentExtensionMethod(method) || method.getParameterTypes().length != 1
                || method.getReturnType() != void.class
                || !lastDexChecksum.equals(blockPropertyMetadataDexChecksum)) return null;
        for (BlockPropertyMetadata property : blockPropertyMetadata) {
            if (("read-write".equals(property.access) || "write-only".equals(property.access))
                    && method.equals(findPropertyAccessor(property, false))) return property;
        }
        return null;
    }

    private static final class BlockArgumentMetadata {
        final String name;
        final String blockType;
        final String defaultValue;
        final String elementType;
        final String elementFormat;
        final String minimum;
        final String maximum;

        BlockArgumentMetadata(String name, String blockType, String defaultValue,
                              String elementType, String elementFormat, String minimum, String maximum) {
            this.name = name;
            this.blockType = blockType;
            this.defaultValue = defaultValue;
            this.elementType = elementType;
            this.elementFormat = elementFormat;
            this.minimum = minimum;
            this.maximum = maximum;
        }
    }

    private static final class ProviderArgumentInput {
        final int parameterIndex;
        final Class<?> javaType;
        final String label;
        final BlockArgumentMetadata metadata;
        String value;
        boolean hasVerifiedDefault;

        ProviderArgumentInput(int parameterIndex, Class<?> javaType, String label,
                              BlockArgumentMetadata metadata, String value,
                              boolean hasVerifiedDefault) {
            this.parameterIndex = parameterIndex;
            this.javaType = javaType;
            this.label = label;
            this.metadata = metadata;
            this.value = value;
            this.hasVerifiedDefault = hasVerifiedDefault;
        }
    }

    private static final class ProviderConfiguration {
        final int mainArgumentIndex;
        final Class<?> targetType;
        final Method providerMethod;
        final List<ProviderArgumentInput> inputs = new ArrayList<ProviderArgumentInput>();
        final List<EditText> editors = new ArrayList<EditText>();

        ProviderConfiguration(int mainArgumentIndex, Class<?> targetType, Method providerMethod) {
            this.mainArgumentIndex = mainArgumentIndex;
            this.targetType = targetType;
            this.providerMethod = providerMethod;
        }
    }

    private static final class ProviderSelectionSlot {
        final int argumentIndex;
        final Class<?> targetType;
        final String label;
        final List<Method> candidates;
        final Spinner spinner;

        ProviderSelectionSlot(int argumentIndex, Class<?> targetType, String label,
                              List<Method> candidates, Spinner spinner) {
            this.argumentIndex = argumentIndex;
            this.targetType = targetType;
            this.label = label;
            this.candidates = candidates;
            this.spinner = spinner;
        }
    }

    private boolean isVerifiedPropertySetter(Method method) {
        return findVerifiedPropertySetter(method) != null;
    }
    private boolean validateSelectedPropertySources(Method method) {
        if (!isCurrentExtensionMethod(method)) {
            String message = "The selected method belongs to a stale DEX/class loader. Select the method again.";
            recordGuideExecutionFailure(method != null ? method.getName() : "", message);
            logToConsole("[STALE-METHOD] " + message);
            return false;
        }
        for (BlockPropertyMetadata property : blockPropertyMetadata) {
            if (!property.name.equals(method.getName())) continue;
            Method verifiedAccessor = null;
            if (method.getParameterTypes().length == 0 && method.getReturnType() != void.class) {
                verifiedAccessor = findPropertyAccessor(property, true);
            } else if (method.getParameterTypes().length == 1 && method.getReturnType() == void.class) {
                verifiedAccessor = findPropertyAccessor(property, false);
            } else {
                continue;
            }
            if (verifiedAccessor == null || !method.equals(verifiedAccessor)) {
                String message = "Method " + formatMethodIdentity(method)
                        + " does not have one unambiguous accessor match in the verified property metadata.";
                recordGuideExecutionFailure(method.getName(), message);
                logToConsole("[PROPERTY-ERR] " + message);
                return false;
            }
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        int targetOffset = parameterTypes.length > 0
                && (AndroidViewComponent.class.isAssignableFrom(parameterTypes[0])
                || View.class.isAssignableFrom(parameterTypes[0])) ? 1 : 0;
        for (int i = targetOffset; i < parameterTypes.length; i++) {
            int argumentIndex = i - targetOffset;
            String key = getPropertySelectionKey(method, argumentIndex);
            String getterName = selectedGetterProperties.get(key);
            if (getterName != null) {
                BlockPropertyMetadata property = findBlockProperty(getterName);
                Method getter = findPropertyAccessor(property, true);
                if (getter == null || !isBlockParameterTypeCompatible(property.type, parameterTypes[i])) {
                    String message = "Getter " + getterName
                            + " no longer has one verified accessor compatible with "
                            + parameterTypes[i].getSimpleName() + ". Choose Literal or select a valid Getter.";
                    recordGuideExecutionFailure(method.getName(), message);
                    logToConsole("[GETTER-ERR] " + message);
                    return false;
                }
            } else {
                selectedGetterProperties.remove(key);
            }
            if (!lastDexChecksum.equals(blockPropertyMetadataDexChecksum)
                    && selectedGetterProperties.containsKey(key)) {
                String message = "Getter metadata does not match the current DEX. Choose Literal input.";
                recordGuideExecutionFailure(method.getName(), message);
                logToConsole("[GETTER-ERR] " + message);
                return false;
            }
        }
        return true;
    }

    public synchronized void updateArgAtPos(int index, String newVal) {
        if (argsEditText == null) return;
        try {
            String current = argsEditText.getText().toString();
            List<String> list = parseArgumentTokens(current);
            while (list.size() <= index) {
                list.add("0");
            }
            list.set(index, formatArgumentToken(newVal != null ? newVal.trim() : ""));

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                sb.append(list.get(i));
                if (i < list.size() - 1) sb.append(", ");
            }
            argsEditText.setText(sb.toString());
        } catch (Exception ignored) {}
    }

    // =========================================================================
    // युनिव्हर्सल सेमेंटिक पॅरामीटर इन्फरन्स इंजिन (कोणत्याही रँडम एक्स्टेंशनसाठी)
    // =========================================================================
    public List<String> extractParamNames(Method method) {
        List<String> names = new ArrayList<String>();
        if (method == null) return names;

        Class<?>[] types = method.getParameterTypes();
        List<String> blockNames = blockParameterNamesByMethod.get(formatMethodIdentity(method));
        if (blockNames != null && blockNames.size() == types.length) {
            names.addAll(blockNames);
            return names;
        }

        Object[] reflectedParameters = null;
        Method getParameters = null;
        Method isNamePresent = null;
        Method getName = null;
        try {
            getParameters = Method.class.getMethod("getParameters");
            Class<?> parameterClass = Class.forName("java.lang.reflect.Parameter");
            isNamePresent = parameterClass.getMethod("isNamePresent");
            getName = parameterClass.getMethod("getName");
            reflectedParameters = (Object[]) getParameters.invoke(method);
        } catch (Exception ignored) {
            reflectedParameters = null;
        }

        for (int i = 0; i < types.length; i++) {
            String name = null;
            if (reflectedParameters != null && i < reflectedParameters.length) {
                try {
                    Object parameter = reflectedParameters[i];
                    if (Boolean.TRUE.equals(isNamePresent.invoke(parameter))) {
                        name = String.valueOf(getName.invoke(parameter));
                    }
                } catch (Exception ignored) {}
            }
            if (name == null || name.isEmpty()) {
                name = "Parameter " + (i + 1) + " (" + types[i].getSimpleName() + ")";
            }
            names.add(name);
        }
        return names;
    }

    private String formatParameterLabel(Method method, int index) {
        Class<?>[] types = method.getParameterTypes();
        BlockPropertyMetadata setterProperty = findVerifiedPropertySetter(method);
        if (setterProperty != null && index == 0) return tx("guide.dynamic.value", setterProperty.type);
        if (index < 0 || index >= types.length) return tx("guide.dynamic.parameter", index + 1);
        List<String> names = extractParamNames(method);
        String name = index < names.size() ? names.get(index) : null;
        String typeName = types[index].getSimpleName();
        if (name == null || name.trim().isEmpty()) return tx("guide.dynamic.parameter", index + 1) + " (" + typeName + ")";
        return name + " (" + typeName + ")";
    }

    private String formatCompactMethodLabel(Method method) {
        if (method == null) return "";
        int sameNameCount = 0;
        for (Method candidate : currentLoadedMethodEntries) {
            if (candidate.getName().equals(method.getName())) sameNameCount++;
        }
        if (sameNameCount < 2) return method.getName();
        return method.getName() + " · " + formatMethodParametersCompact(method);
    }

    private String formatMethodParametersCompact(Method method) {
        StringBuilder result = new StringBuilder("(");
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) result.append(", ");
            result.append(types[i].getSimpleName());
        }
        return result.append(')').toString();
    }

    private void showSelectedMethodDetails(Method method) {
        if (method == null || activity == null) return;
        StringBuilder details = new StringBuilder();
        details.append(method.getDeclaringClass().getName()).append('\n');
        details.append(method.getReturnType().getName()).append(' ')
                .append(method.getName()).append('(');
        Class<?>[] types = method.getParameterTypes();
        List<String> parameterNames = extractParamNames(method);
        for (int i = 0; i < types.length; i++) {
            if (i > 0) details.append(",\n  ");
            else details.append("\n  ");
            details.append(types[i].getName()).append(' ');
            if (i < parameterNames.size()) details.append(parameterNames.get(i));
            else details.append("Parameter ").append(i + 1);
        }
        if (types.length > 0) details.append('\n');
        details.append(')');

        boolean authoritativeReflectionNames = types.length > 0;
        for (String name : parameterNames) {
            if (name.startsWith("Parameter ")) authoritativeReflectionNames = false;
        }
        boolean verifiedBlockNames = blockParameterNamesByMethod.containsKey(formatMethodIdentity(method));
        details.append("\n\nParameter names: ").append(types.length == 0
                ? "no parameters"
                : verifiedBlockNames
                        ? "matched from verified extension block metadata"
                        : authoritativeReflectionNames
                                ? "available from Java reflection metadata"
                                : "not present in the transferred DEX");
        details.append("\nBlock metadata: ").append(verifiedBlockNames
                ? "verified against the loaded class and DEX checksum"
                : "unavailable for this DEX");

        TextView body = new TextView(activity);
        body.setText(details.toString());
        body.setTextSize(12f);
        body.setTextColor(Color.parseColor("#1E293B"));
        body.setPadding(dipToPx(18), dipToPx(12), dipToPx(18), dipToPx(12));
        body.setTextIsSelectable(true);
        ScrollView scroll = new ScrollView(activity);
        scroll.addView(body);
        new android.app.AlertDialog.Builder(activity)
                .setTitle(formatCompactMethodLabel(method))
                .setView(scroll)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private boolean isColorParameter(String paramName, String methodName, Class<?> pType, String curVal) {
        String pClean = paramName != null ? paramName.toLowerCase(Locale.ROOT) : "";
        String mClean = methodName != null ? methodName.toLowerCase(Locale.ROOT) : "";
        String vClean = curVal != null ? curVal.trim() : "";

        if (vClean.startsWith("#")) return true;
        if (pClean.contains("color") || pClean.contains("hex") || pClean.contains("tint")) return true;
        if (pClean.equals("cardcolor") || pClean.equals("backgroundcolor") || pClean.equals("strokecolor")) return true;
        if (mClean.contains("color") && (pType == int.class || pType == Integer.class || pType == Object.class || pType == String.class)) return true;
        if (mClean.contains("gradient") && (pClean.contains("start") || pClean.contains("end") || pClean.contains("center"))) return true;
        if (mClean.contains("neumorph") && (pClean.contains("card") || pClean.contains("color") || pClean.contains("background"))) return true;
        return false;
    }

    public String getSmartDefaultValue(String paramName, Class<?> pType, int userIdx) {
        return getSmartDefaultValue(paramName, pType, userIdx, userSelectedMethod);
    }

    public String getSmartDefaultValue(String paramName, Class<?> pType, int userIdx, String methodName) {
        String clean = paramName != null ? paramName.toLowerCase(Locale.ROOT) : "";
        String mLower = methodName != null ? methodName.toLowerCase(Locale.ROOT) : "";

        // १. रंगासाठी डीफॉल्ट्स
        if (isColorParameter(paramName, methodName, pType, "")) {
            if (mLower.contains("neumorph")) return "#E0E5EC";
            if (clean.contains("end")) return "#0EA5E9";
            if (clean.contains("center")) return "#6366F1";
            if (clean.contains("background") || mLower.contains("background")) return "#1E293B";
            if (clean.contains("stroke") || clean.contains("border")) return "#F43F5E";
            if (clean.contains("shadow")) return "#000000";
            return userIdx == 1 ? "#EF4444" : "#10B981";
        }
        // २. वेळ आणि ॲनिमेशन
        if (clean.contains("duration") || clean.contains("time") || clean.contains("ms") || clean.contains("speed")) {
            return "1000";
        }
        // ३. बुलियन
        if (pType == boolean.class || pType == Boolean.class || clean.contains("animate") || clean.contains("flat") || clean.contains("enable") || clean.contains("visible")) {
            return "true";
        }
        // ४. स्टाईल / शेप / लाइटसोर्स (Getters मधून शोधणे)
        if (clean.contains("style") || clean.contains("shape") || clean.contains("mode") || clean.contains("light")) {
            List<String> getters = getAvailableGetters();
            if (!getters.isEmpty()) {
                for (String g : getters) {
                    if (g.toLowerCase(Locale.ROOT).contains(clean)) return g;
                }
                return getters.get(0);
            }
            return "1";
        }
        // ५. परिमाणे व संख्या
        if (clean.contains("layercount") || clean.contains("count")) {
            return "4";
        }
        if (clean.contains("spacing") || clean.contains("gap")) {
            return "24";
        }
        if (clean.contains("strokewidth") || clean.contains("width") || clean.contains("thickness")) {
            return "4";
        }
        if (clean.contains("radius") || clean.contains("corner")) {
            return "16";
        }
        if (clean.contains("elevation") || clean.contains("depth")) {
            return "10";
        }
        if (clean.contains("alpha") || clean.contains("opacity")) {
            return (pType == float.class || pType == Float.class || pType == double.class) ? "0.8" : "200";
        }
        if (clean.contains("itemid") || clean.contains("index") || clean.contains("tab") || clean.contains("pos")) {
            return "1";
        }
        // ६. मजकूर
        if (clean.contains("icon")) {
            return "favorite";
        }
        if (clean.contains("title")) {
            return "Home";
        }
        if (clean.contains("font")) {
            return "sans-serif";
        }
        if (clean.contains("url")) {
            return "https://google.com";
        }
        if (pType == String.class) {
            return "Welcome to Bolt";
        }
        if (pType == int.class || pType == Integer.class || pType == long.class) {
            return "10";
        }
        if (pType == float.class || pType == Float.class || pType == double.class) {
            return "10.0";
        }
        if (YailList.class.isAssignableFrom(pType) || List.class.isAssignableFrom(pType)) {
            return "[1, 2, 3]";
        }
        return "10";
    }

    private String generateSmartDefaultCSV(Method m) {
        if (m == null) return "";
        Class<?>[] pTypes = m.getParameterTypes();
        List<String> paramNames = extractParamNames(m);

        int offset = 0;
        if (pTypes.length > 0 && (AndroidViewComponent.class.isAssignableFrom(pTypes[0]) || View.class.isAssignableFrom(pTypes[0]))) {
            offset = 1;
        }

        StringBuilder sb = new StringBuilder();
        int userIdx = 0;
        for (int i = offset; i < pTypes.length; i++) {
            userIdx++;
            String pName = (i < paramNames.size()) ? paramNames.get(i) : ("param" + userIdx);
            String defVal = getSmartDefaultValue(pName, pTypes[i], userIdx, m.getName());
            sb.append(formatArgumentToken(defVal));
            if (i < pTypes.length - 1) sb.append(", ");
        }
        return sb.toString();
    }

    private String readOptionalMetadataValue(JSONObject object, String key) {
        if (object == null || key == null || !object.has(key) || object.isNull(key)) return null;
        Object value = object.opt(key);
        return value != null ? String.valueOf(value) : null;
    }

    private String firstOptionalMetadataValue(JSONObject object, String first, String second) {
        String value = readOptionalMetadataValue(object, first);
        return value != null && !value.trim().isEmpty()
                ? value.trim() : readOptionalMetadataValue(object, second);
    }

    private BlockArgumentMetadata getBlockArgumentMetadata(Method method, int parameterIndex) {
        if (method == null) return null;
        List<BlockArgumentMetadata> metadata = blockArgumentMetadataByMethod.get(formatMethodIdentity(method));
        return metadata != null && parameterIndex >= 0 && parameterIndex < metadata.size()
                ? metadata.get(parameterIndex) : null;
    }

    private String getTypedBlockDefault(Class<?> type, BlockArgumentMetadata metadata) {
        if (type == null) return null;
        if (metadata != null && metadata.defaultValue != null) {
            try {
                convertArgument(metadata.defaultValue, type);
                return metadata.defaultValue;
            } catch (Exception ignored) {
                logToConsole("[BLOCK-DEFAULT] Ignored metadata default incompatible with " + type.getName() + ".");
            }
        }

        if (type == boolean.class || type == Boolean.class) return "false";
        if (type == byte.class || type == Byte.class || type == short.class || type == Short.class
                || type == int.class || type == Integer.class || type == long.class || type == Long.class) {
            return getBoundedNumericDefault(type, metadata, "0");
        }
        if (type == float.class || type == Float.class || type == double.class || type == Double.class) {
            return getBoundedNumericDefault(type, metadata, "0.0");
        }
        if (type == String.class) return "";
        if (YailList.class.isAssignableFrom(type) || List.class.isAssignableFrom(type)) return "[]";
        return null;
    }

    private String getBoundedNumericDefault(Class<?> type, BlockArgumentMetadata metadata, String neutralValue) {
        String minimum = metadata != null ? metadata.minimum : null;
        String maximum = metadata != null ? metadata.maximum : null;
        try {
            java.math.BigDecimal zero = new java.math.BigDecimal(neutralValue);
            java.math.BigDecimal min = minimum != null ? new java.math.BigDecimal(minimum) : null;
            java.math.BigDecimal max = maximum != null ? new java.math.BigDecimal(maximum) : null;
            java.math.BigDecimal candidate = zero;
            if (min != null && candidate.compareTo(min) < 0) candidate = min;
            if (max != null && candidate.compareTo(max) > 0) candidate = max;
            String result = candidate.stripTrailingZeros().toPlainString();
            convertArgument(result, type);
            return result;
        } catch (Exception ignored) {
            return neutralValue;
        }
    }

    private boolean isDeclaredColorListMetadata(BlockArgumentMetadata metadata) {
        if (metadata == null) return false;
        String elementType = metadata.elementType != null ? metadata.elementType.toLowerCase(Locale.ROOT) : "";
        String blockType = metadata.blockType != null ? metadata.blockType.toLowerCase(Locale.ROOT) : "";
        return "color".equals(elementType) || "colour".equals(elementType)
                || "color-list".equals(blockType) || "colour-list".equals(blockType)
                || "list<color>".equals(blockType) || "list<colour>".equals(blockType);
    }

    private boolean isVerifiedColorListMetadata(BlockArgumentMetadata metadata) {
        if (metadata == null) return false;
        String elementType = metadata.elementType != null ? metadata.elementType.toLowerCase(Locale.ROOT) : "";
        String blockType = metadata.blockType != null ? metadata.blockType.toLowerCase(Locale.ROOT) : "";
        boolean verifiedBlockType = "color-list".equals(blockType) || "colour-list".equals(blockType)
                || "list<color>".equals(blockType) || "list<colour>".equals(blockType);
        return ("color".equals(elementType) || "colour".equals(elementType)) && verifiedBlockType;
    }

    private int getVerifiedColorListElementFormat(BlockArgumentMetadata metadata) {
        if (!isVerifiedColorListMetadata(metadata)) return 0;
        String declaredFormat = metadata.elementFormat != null
                ? metadata.elementFormat.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "")
                : "";
        if ("string".equals(declaredFormat) || "hex".equals(declaredFormat)
                || "hexstring".equals(declaredFormat) || "text".equals(declaredFormat)) return 1;
        if ("argb".equals(declaredFormat) || "argbint".equals(declaredFormat)
                || "androidcolor".equals(declaredFormat) || "colorint".equals(declaredFormat)
                || "colourint".equals(declaredFormat)) return 2;
        if ("rgb".equals(declaredFormat) || "rgb24".equals(declaredFormat)
                || "rgbint".equals(declaredFormat)) return 3;
        return 0;
    }

    private void addUnverifiedColorListMetadataNotice(LinearLayout parent, Class<?> parameterType,
                                                       BlockArgumentMetadata metadata) {
        if (parent == null || parameterType == null || !YailList.class.isAssignableFrom(parameterType)
                || getVerifiedColorListElementFormat(metadata) != 0) return;
        String elementType = metadata != null && metadata.elementType != null
                ? metadata.elementType.trim() : "";
        String elementFormat = metadata != null && metadata.elementFormat != null
                ? metadata.elementFormat.trim() : "";
        TextView notice = new TextView(activity);
        String blockType = metadata != null && metadata.blockType != null
                ? metadata.blockType.trim() : "";
        boolean colorElement = "color".equalsIgnoreCase(elementType) || "colour".equalsIgnoreCase(elementType);
        boolean colorBlock = "color-list".equalsIgnoreCase(blockType) || "colour-list".equalsIgnoreCase(blockType)
                || "list<color>".equalsIgnoreCase(blockType) || "list<colour>".equalsIgnoreCase(blockType);
        if (!colorElement || !colorBlock) {
            notice.setText("Color list not verified: metadata must declare elementType=color and blockType=color-list.");
        } else if (elementFormat.isEmpty()) {
            notice.setText("Color list format missing: metadata must declare elementFormat (hex-string, argb-int, or rgb-int).");
        } else {
            notice.setText("Unsupported color-list elementFormat '" + elementFormat + "'. Picker and typed conversion are disabled.");
        }
        notice.setTextColor(Color.parseColor("#92400E"));
        notice.setTextSize(9f);
        notice.setPadding(0, dipToPx(3), 0, dipToPx(5));
        parent.addView(notice);
    }

    private boolean isValidHexColor(String value) {
        if (value == null || !value.matches("#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?")) return false;
        try { Color.parseColor(value); return true; } catch (Exception ignored) { return false; }
    }

    private String normalizeVerifiedHexColor(String value) {
        if (value == null) throw new IllegalArgumentException("Color value is empty.");
        String normalized = value.trim();
        if (!normalized.startsWith("#")) normalized = "#" + normalized;
        if (!isValidHexColor(normalized)) {
            throw new IllegalArgumentException("Invalid HEX color '" + value
                    + "'. Expected six or eight hexadecimal digits.");
        }
        return normalized.toUpperCase(Locale.ROOT);
    }

    private YailList convertVerifiedColorList(ProviderArgumentInput input) {
        return convertVerifiedColorList(input != null ? input.value : null,
                input != null ? input.metadata : null);
    }

    private YailList convertVerifiedColorList(String rawValue, BlockArgumentMetadata metadata) {
        int elementFormat = getVerifiedColorListElementFormat(metadata);
        if (elementFormat == 0) {
            throw new IllegalArgumentException("Color-list element format is not verified by metadata.");
        }
        List<Object> source = new ArrayList<Object>();
        String raw = rawValue != null ? rawValue.trim() : "";
        if (raw.startsWith("[") && raw.endsWith("]")) {
            try {
                JSONArray values = new JSONArray(raw);
                for (int i = 0; i < values.length(); i++) source.add(values.get(i));
            } catch (Exception e) {
                throw new IllegalArgumentException("Provider color list is not valid JSON.", e);
            }
        } else {
            for (String item : splitDelimitedValues(raw, true)) {
                if (!item.trim().isEmpty()) source.add(item.trim());
            }
        }
        if (source.isEmpty()) {
            if (metadata.defaultValue != null) {
                try {
                    JSONArray verifiedDefault = new JSONArray(metadata.defaultValue);
                    if (verifiedDefault.length() == 0) return YailList.makeList(source);
                } catch (Exception ignored) {}
            }
            throw new IllegalArgumentException("Choose at least one color; an empty list is allowed only when the verified API default is empty.");
        }

        List<Object> converted = new ArrayList<Object>();
        for (int i = 0; i < source.size(); i++) {
            Object value = source.get(i);
            if (elementFormat == 1) {
                if (!(value instanceof String)) {
                    throw new IllegalArgumentException("Color " + (i + 1)
                            + " must be a HEX string according to the verified metadata.");
                }
                converted.add(normalizeVerifiedHexColor((String) value));
            } else {
                final int color;
                if (value instanceof Number) {
                    double numeric = ((Number) value).doubleValue();
                    long integer = ((Number) value).longValue();
                    long maximum = elementFormat == 3 ? 0xFFFFFFL : 0xFFFFFFFFL;
                    if (Double.isNaN(numeric) || Double.isInfinite(numeric)
                            || numeric != (double) integer || integer < (elementFormat == 3 ? 0L : Integer.MIN_VALUE)
                            || integer > maximum) {
                        throw new IllegalArgumentException("Color " + (i + 1)
                                + " is outside the verified integer color range.");
                    }
                    color = (int) integer;
                } else if (value instanceof String) {
                    color = Color.parseColor(normalizeVerifiedHexColor((String) value));
                } else {
                    throw new IllegalArgumentException("Color " + (i + 1)
                            + " is not a supported HEX or integer value.");
                }
                converted.add(elementFormat == 3 ? (color & 0xFFFFFF) : color);
            }
        }
        return YailList.makeList(converted);
    }

    private Object convertMethodArgument(String rawValue, Method method, int parameterIndex,
                                         Class<?> targetType) {
        BlockArgumentMetadata metadata = getBlockArgumentMetadata(method, parameterIndex);
        if (YailList.class.isAssignableFrom(targetType) && isDeclaredColorListMetadata(metadata)) {
            if (getVerifiedColorListElementFormat(metadata) == 0) {
                throw new IllegalArgumentException("Color-list metadata is incomplete or unsupported; expected elementType, blockType, and elementFormat.");
            }
            return convertVerifiedColorList(rawValue, metadata);
        }
        return convertArgument(rawValue, targetType);
    }

    private String encodeVerifiedColorList(List<String> colors, BlockArgumentMetadata metadata) {
        int elementFormat = getVerifiedColorListElementFormat(metadata);
        if (elementFormat == 0) throw new IllegalArgumentException("Color-list element format is not verified by metadata.");
        JSONArray values = new JSONArray();
        if (colors != null) {
            for (String color : colors) {
                if (color == null || color.trim().isEmpty()) continue;
                String normalized = normalizeVerifiedHexColor(color);
                if (elementFormat == 1) values.put(normalized);
                else {
                    int parsed = Color.parseColor(normalized);
                    values.put(elementFormat == 3 ? parsed & 0xFFFFFF : parsed);
                }
            }
        }
        return values.toString();
    }

    private List<String> readVerifiedColorList(String rawValue, int elementFormat) {
        List<String> colors = new ArrayList<String>();
        if (rawValue == null || rawValue.trim().isEmpty()) return colors;
        List<Object> values = new ArrayList<Object>();
        String raw = rawValue.trim();
        if (raw.startsWith("[") && raw.endsWith("]")) {
            try {
                JSONArray array = new JSONArray(raw);
                for (int i = 0; i < array.length(); i++) values.add(array.get(i));
            } catch (Exception ignored) { return colors; }
        } else {
            for (String item : splitDelimitedValues(raw, true)) if (!item.trim().isEmpty()) values.add(item.trim());
        }
        for (Object value : values) {
            try {
                if (elementFormat == 1 && !(value instanceof String)) continue;
                String hex = value instanceof Number
                        ? (elementFormat == 3
                            ? String.format(Locale.ROOT, "#%06X", ((Number) value).intValue() & 0xFFFFFF)
                            : String.format(Locale.ROOT, "#%08X", ((Number) value).intValue()))
                        : normalizeVerifiedHexColor(String.valueOf(value));
                if (isValidHexColor(hex)) colors.add(hex.toUpperCase(Locale.ROOT));
            } catch (Exception ignored) {}
        }
        return colors;
    }

    private String generateTypedBlockDefaults(Method method) {
        if (method == null) return "";
        Class<?>[] types = method.getParameterTypes();
        int offset = types.length > 0 && (AndroidViewComponent.class.isAssignableFrom(types[0])
                || View.class.isAssignableFrom(types[0])) ? 1 : 0;
        StringBuilder result = new StringBuilder();
        for (int i = offset; i < types.length; i++) {
            BlockArgumentMetadata metadata = getBlockArgumentMetadata(method, i);
            String value = getTypedBlockDefault(types[i], metadata);
            result.append(formatArgumentToken(value != null ? value : ""));
            if (i < types.length - 1) result.append(", ");
        }
        return result.toString();
    }

    private boolean isProviderReturnTypeCompatible(Class<?> returnType, Class<?> targetType) {
        if (returnType == null || targetType == null || returnType == void.class || returnType == Void.class) return false;
        if (targetType == char.class) return false;
        Class<?> boxedReturn = boxPrimitiveType(returnType);
        Class<?> boxedTarget = boxPrimitiveType(targetType);
        if (boxedTarget == Object.class || boxedTarget.isAssignableFrom(boxedReturn)) return true;
        return isNumericType(boxedReturn) && isNumericType(boxedTarget);
    }

    private boolean isSupportedProviderArgumentType(Class<?> type) {
        if (type == null || type == char.class || type == Character.class
                || View.class.isAssignableFrom(type) || AndroidViewComponent.class.isAssignableFrom(type)
                || Component.class.isAssignableFrom(type)) return false;
        return type == Object.class || type == String.class || type == boolean.class || type == Boolean.class
                || isNumericType(type) || YailList.class.isAssignableFrom(type)
                || List.class.isAssignableFrom(type) || YailDictionary.class.isAssignableFrom(type)
                || Map.class.isAssignableFrom(type);
    }

    private boolean isVerifiedProviderMethod(Method method) {
        String identity = method != null ? formatMethodIdentity(method) : "";
        if (blockParameterNamesByMethod.containsKey(identity)
                && blockArgumentMetadataByMethod.containsKey(identity)) return true;
        if (method == null || method.getParameterTypes().length != 0 || method.getReturnType() == void.class
                || !lastDexChecksum.equals(blockPropertyMetadataDexChecksum)) return false;
        for (BlockPropertyMetadata property : blockPropertyMetadata) {
            if ("read-only".equals(property.access) || "read-write".equals(property.access)) {
                if (method.equals(findPropertyAccessor(property, true))) return true;
            }
        }
        return false;
    }

    private Class<?> boxPrimitiveType(Class<?> type) {
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == char.class) return Character.class;
        return type;
    }

    private List<Method> getVerifiedProviderCandidates(Method mainMethod, Class<?> targetType) {
        List<Method> candidates = new ArrayList<Method>();
        if (mainMethod == null || targetType == null) return candidates;
        for (Method candidate : currentLoadedMethodEntries) {
            if (candidate == null || candidate.equals(mainMethod) || candidate.isBridge() || candidate.isSynthetic()
                    || candidate.getDeclaringClass() != currentExtensionClass
                    || !isVerifiedProviderMethod(candidate)) continue;
            Class<?>[] providerTypes = candidate.getParameterTypes();
            int offset = providerTypes.length > 0 && (AndroidViewComponent.class.isAssignableFrom(providerTypes[0])
                    || View.class.isAssignableFrom(providerTypes[0])) ? 1 : 0;
            boolean supportedInputs = true;
            for (int i = offset; i < providerTypes.length; i++) {
                if (!isSupportedProviderArgumentType(providerTypes[i])) {
                    supportedInputs = false;
                    break;
                }
            }
            if (!supportedInputs) continue;
            if (isProviderReturnTypeCompatible(candidate.getReturnType(), targetType)) candidates.add(candidate);
        }
        return candidates;
    }

    private String getProviderValueCacheKey(Method mainMethod, int argumentIndex, Method providerMethod) {
        return lastDexChecksum + "|" + formatMethodIdentity(mainMethod) + "|" + argumentIndex
                + "|" + formatMethodIdentity(providerMethod);
    }

    private ProviderConfiguration createProviderConfiguration(Method mainMethod, int argumentIndex,
                                                                 Class<?> targetType, Method providerMethod) {
        ProviderConfiguration configuration = new ProviderConfiguration(argumentIndex, targetType, providerMethod);
        Class<?>[] providerTypes = providerMethod.getParameterTypes();
        int offset = providerTypes.length > 0 && (AndroidViewComponent.class.isAssignableFrom(providerTypes[0])
                || View.class.isAssignableFrom(providerTypes[0])) ? 1 : 0;
        List<String> cachedValues = providerArgumentValueCache.get(
                getProviderValueCacheKey(mainMethod, argumentIndex, providerMethod));
        for (int parameterIndex = offset; parameterIndex < providerTypes.length; parameterIndex++) {
            BlockArgumentMetadata metadata = getBlockArgumentMetadata(providerMethod, parameterIndex);
            String name = metadata != null && metadata.name != null && !metadata.name.trim().isEmpty()
                    ? metadata.name : "Parameter " + (parameterIndex - offset + 1);
            String defaultValue = getVerifiedProviderDefault(providerTypes[parameterIndex], metadata);
            boolean hasDefault = defaultValue != null;
            String value = cachedValues != null && parameterIndex - offset < cachedValues.size()
                    ? cachedValues.get(parameterIndex - offset) : (hasDefault ? defaultValue : "");
            configuration.inputs.add(new ProviderArgumentInput(parameterIndex, providerTypes[parameterIndex],
                    name, metadata, value, hasDefault));
        }
        return configuration;
    }

    private void saveProviderArgumentValues(ProviderConfiguration configuration, Method mainMethod) {
        if (configuration == null || mainMethod == null) return;
        List<String> values = new ArrayList<String>();
        for (ProviderArgumentInput input : configuration.inputs) values.add(input.value);
        providerArgumentValueCache.put(getProviderValueCacheKey(mainMethod,
                configuration.mainArgumentIndex, configuration.providerMethod), values);
    }

    private String getVerifiedProviderDefault(Class<?> type, BlockArgumentMetadata metadata) {
        if (metadata == null || metadata.defaultValue == null || metadata.defaultValue.trim().isEmpty()) return null;
        try {
            if (YailList.class.isAssignableFrom(type) && isDeclaredColorListMetadata(metadata)) {
                if (getVerifiedColorListElementFormat(metadata) == 0) return null;
                convertVerifiedColorList(metadata.defaultValue, metadata);
                return metadata.defaultValue;
            }
            convertArgument(metadata.defaultValue, type);
            return metadata.defaultValue;
        } catch (Exception ignored) {
            logToConsole("[PROVIDER-DEFAULT] Ignored incompatible metadata default for "
                    + (metadata.name != null ? metadata.name : type.getSimpleName()) + ".");
            return null;
        }
    }

    private void refreshProviderSelectionUi(Method method) {
        providerSelectionSlots.clear();
        String methodIdentity = method != null ? lastDexChecksum + "|" + formatMethodIdentity(method) : "";
        if (!methodIdentity.equals(providerSelectionMainMethod)) {
            selectedProviderConfigurations.clear();
            providerSelectionMainMethod = methodIdentity;
        }
        if (method == null || !isCurrentExtensionMethod(method)) {
            return;
        }

        Class<?>[] mainTypes = method.getParameterTypes();
        int mainOffset = mainTypes.length > 0 && (AndroidViewComponent.class.isAssignableFrom(mainTypes[0])
                || View.class.isAssignableFrom(mainTypes[0])) ? 1 : 0;
        for (int parameterIndex = mainOffset; parameterIndex < mainTypes.length; parameterIndex++) {
            int userArgumentIndex = parameterIndex - mainOffset;
            if (selectedGetterProperties.containsKey(getPropertySelectionKey(method, userArgumentIndex))) continue;
            List<Method> candidates = getVerifiedProviderCandidates(method, mainTypes[parameterIndex]);
            if (candidates.isEmpty()) continue;
            ProviderConfiguration existing = selectedProviderConfigurations.get(userArgumentIndex);
            if (existing != null && (existing.targetType != mainTypes[parameterIndex]
                    || !candidates.contains(existing.providerMethod))) {
                selectedProviderConfigurations.remove(userArgumentIndex);
                existing = null;
            }

            final ProviderSelectionSlot slot = new ProviderSelectionSlot(userArgumentIndex,
                    mainTypes[parameterIndex], formatParameterLabel(method, parameterIndex), candidates, null);
            providerSelectionSlots.add(slot);
        }
    }

    private void renderSelectedProviderViews(final Method mainMethod, LinearLayout container) {
        if (container == null) return;
        container.removeAllViews();
        for (final ProviderSelectionSlot slot : providerSelectionSlots) {
            TextView mappingLabel = new TextView(activity);
            mappingLabel.setText("Provider for " + slot.label + " (choose explicitly)");
            mappingLabel.setTextColor(Color.parseColor("#475569"));
            mappingLabel.setTypeface(null, Typeface.BOLD);
            mappingLabel.setTextSize(10f);
            container.addView(mappingLabel);
            Spinner providerSpinner = new Spinner(activity);
            List<String> options = new ArrayList<String>();
            options.add("Use direct input");
            for (Method candidate : slot.candidates) options.add(formatMethodIdentity(candidate));
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
                    android.R.layout.simple_spinner_item, options);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            providerSpinner.setAdapter(adapter);
            final ProviderConfiguration configuration = selectedProviderConfigurations.get(slot.argumentIndex);
            int selectedPosition = 0;
            if (configuration != null) {
                for (int candidateIndex = 0; candidateIndex < slot.candidates.size(); candidateIndex++) {
                    if (slot.candidates.get(candidateIndex).equals(configuration.providerMethod)) selectedPosition = candidateIndex + 1;
                }
            }
            final int[] lastHandledPosition = new int[]{selectedPosition};
            final LinearLayout parameterContainer = new LinearLayout(activity);
            parameterContainer.setOrientation(LinearLayout.VERTICAL);
            parameterContainer.setPadding(dipToPx(8), dipToPx(6), dipToPx(8), dipToPx(6));
            renderProviderParameters(mainMethod, slot, configuration, parameterContainer);
            providerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // AdapterView reports its initial selection too. Ignore the unchanged
                    // position so attachment cannot rebuild the provider hierarchy.
                    if (position == lastHandledPosition[0]) return;
                    lastHandledPosition[0] = position;
                    if (position <= 0 || position > slot.candidates.size()) {
                        ProviderConfiguration current = selectedProviderConfigurations.get(slot.argumentIndex);
                        if (current != null) saveProviderArgumentValues(current, mainMethod);
                        selectedProviderConfigurations.remove(slot.argumentIndex);
                    } else {
                        Method selected = slot.candidates.get(position - 1);
                        ProviderConfiguration current = selectedProviderConfigurations.get(slot.argumentIndex);
                        if (current == null || !current.providerMethod.equals(selected)
                                || current.targetType != slot.targetType) {
                            if (current != null) saveProviderArgumentValues(current, mainMethod);
                            selectedProviderConfigurations.put(slot.argumentIndex,
                                    createProviderConfiguration(mainMethod, slot.argumentIndex, slot.targetType, selected));
                        }
                    }
                    renderProviderParameters(mainMethod, slot,
                            selectedProviderConfigurations.get(slot.argumentIndex), parameterContainer);
                    if (selectedProviderConfigurations.containsKey(slot.argumentIndex)) {
                        scheduleProviderPreviewIfReady(mainMethod);
                    }
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });
            providerSpinner.setSelection(selectedPosition);
            container.addView(providerSpinner);
            container.addView(parameterContainer);
        }
    }

    private void renderProviderParameters(final Method mainMethod, final ProviderSelectionSlot slot,
                                          final ProviderConfiguration configuration, LinearLayout container) {
        container.removeAllViews();
        if (configuration == null) return;
            TextView title = new TextView(activity);
            title.setText("call " + configuration.providerMethod.getName() + " → " + slot.label);
            title.setTextColor(Color.WHITE);
            title.setTypeface(null, Typeface.BOLD);
            title.setTextSize(11f);
            title.setPadding(dipToPx(8), dipToPx(6), dipToPx(8), dipToPx(6));
            GradientDrawable titleBackground = new GradientDrawable();
            titleBackground.setColor(Color.parseColor("#6C3EB8"));
            titleBackground.setCornerRadius(dipToPx(5));
            title.setBackground(titleBackground);
            container.addView(title);

            TextView explanation = new TextView(activity);
            explanation.setText("The selected method runs first; its converted return value is passed to this input.");
            explanation.setTextColor(Color.parseColor("#64748B"));
            explanation.setTextSize(9f);
            explanation.setPadding(0, dipToPx(3), 0, dipToPx(3));
            container.addView(explanation);

            configuration.editors.clear();
            for (int i = 0; i < configuration.inputs.size(); i++) {
                final ProviderArgumentInput input = configuration.inputs.get(i);
                TextView inputLabel = new TextView(activity);
                inputLabel.setText(input.label + " (" + input.javaType.getSimpleName() + ")");
                inputLabel.setTextColor(Color.parseColor("#334155"));
                inputLabel.setTypeface(null, Typeface.BOLD);
                inputLabel.setTextSize(10f);
                inputLabel.setPadding(0, dipToPx(5), 0, dipToPx(2));
                container.addView(inputLabel);

                if (YailList.class.isAssignableFrom(input.javaType)
                        && getVerifiedColorListElementFormat(input.metadata) != 0) {
                    renderProviderColorListEditor(container, configuration, input, mainMethod);
                    continue;
                }

                EditText editor = new EditText(activity);
                editor.setSingleLine(false);
                editor.setTextSize(11f);
                editor.setTextColor(Color.parseColor("#0F172A"));
                editor.setPadding(dipToPx(8), dipToPx(4), dipToPx(8), dipToPx(4));
                GradientDrawable editorBackground = new GradientDrawable();
                editorBackground.setColor(Color.WHITE);
                editorBackground.setCornerRadius(dipToPx(6));
                editorBackground.setStroke(dipToPx(1), Color.parseColor("#CBD5E1"));
                editor.setBackground(editorBackground);
                if (isIntegerType(input.javaType)) {
                    editor.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                            | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
                } else if (input.javaType == float.class || input.javaType == Float.class
                        || input.javaType == double.class || input.javaType == Double.class) {
                    editor.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                            | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED
                            | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else {
                    editor.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                            | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                }
                if (!input.hasVerifiedDefault && input.value.trim().isEmpty()) {
                    editor.setHint("No verified default; enter a value");
                }
                editor.setText(input.value);
                editor.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                        input.value = s.toString();
                        saveProviderArgumentValues(configuration, mainMethod);
                        scheduleProviderPreviewIfReady(mainMethod);
                    }
                    @Override public void afterTextChanged(Editable s) {}
                });
                configuration.editors.add(editor);
                container.addView(editor);
                addUnverifiedColorListMetadataNotice(container, input.javaType, input.metadata);
            }
    }

    private void writeProviderColorValues(ProviderConfiguration configuration, ProviderArgumentInput input,
                                          List<String> selectedHexValues, Method mainMethod) {
        input.value = encodeVerifiedColorList(selectedHexValues, input.metadata);
        input.hasVerifiedDefault = false;
        for (String hex : selectedHexValues) {
            if (isValidHexColor(hex)) { input.hasVerifiedDefault = true; break; }
        }
        saveProviderArgumentValues(configuration, mainMethod);
    }

    private void renderProviderColorListEditor(final LinearLayout container,
                                               final ProviderConfiguration configuration,
                                               final ProviderArgumentInput input,
                                               final Method mainMethod) {
        final LinearLayout colorsContainer = new LinearLayout(activity);
        colorsContainer.setOrientation(LinearLayout.VERTICAL);
        final List<String> selectedColors = readVerifiedColorList(input.value,
                getVerifiedColorListElementFormat(input.metadata));
        while (selectedColors.size() < 3) selectedColors.add("");

        final Runnable[] renderRows = new Runnable[1];
        renderRows[0] = new Runnable() {
            @Override public void run() {
                colorsContainer.removeAllViews();
                for (int colorIndex = 0; colorIndex < selectedColors.size(); colorIndex++) {
                    final int index = colorIndex;
                    LinearLayout row = new LinearLayout(activity);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.CENTER_VERTICAL);
                    row.setPadding(0, dipToPx(3), 0, dipToPx(3));
                    final View preview = new View(activity);
                    GradientDrawable previewBackground = new GradientDrawable();
                    previewBackground.setCornerRadius(dipToPx(5));
                    previewBackground.setStroke(dipToPx(1), Color.parseColor("#94A3B8"));
                    String current = selectedColors.get(index);
                    previewBackground.setColor(isValidHexColor(current) ? Color.parseColor(current) : Color.TRANSPARENT);
                    preview.setBackground(previewBackground);
                    row.addView(preview, new LinearLayout.LayoutParams(dipToPx(38), dipToPx(32)));

                    TextView hexLabel = new TextView(activity);
                    hexLabel.setText(isValidHexColor(current) ? current : "Choose color");
                    hexLabel.setTextColor(Color.parseColor("#334155"));
                    hexLabel.setTextSize(10f);
                    hexLabel.setPadding(dipToPx(8), 0, dipToPx(5), 0);
                    row.addView(hexLabel, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                    Button choose = new Button(activity);
                    choose.setText("Pick");
                    applyRoundedButtonStyle(choose, Color.parseColor("#EDE9FE"), Color.parseColor("#5B21B6"), 6);
                    choose.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            showProviderColorPicker(selectedColors.get(index), new ColorSelectionCallback() {
                                @Override public void onColorSelected(String hex) {
                                    selectedColors.set(index, hex);
                                    writeProviderColorValues(configuration, input, selectedColors, mainMethod);
                                    renderRows[0].run();
                                    scheduleProviderPreviewIfReady(mainMethod);
                                }
                            });
                        }
                    });
                    row.addView(choose, new LinearLayout.LayoutParams(dipToPx(66), dipToPx(38)));

                    {
                        Button remove = new Button(activity);
                        remove.setText("−");
                        applyRoundedButtonStyle(remove, Color.parseColor("#FEE2E2"), Color.parseColor("#991B1B"), 6);
                        remove.setOnClickListener(new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                selectedColors.remove(index);
                                writeProviderColorValues(configuration, input, selectedColors, mainMethod);
                                renderRows[0].run();
                                scheduleProviderPreviewIfReady(mainMethod);
                            }
                        });
                        row.addView(remove, new LinearLayout.LayoutParams(dipToPx(42), dipToPx(38)));
                    }
                    colorsContainer.addView(row);
                }
                Button addColor = new Button(activity);
                addColor.setText("+ Add Color");
                applyRoundedButtonStyle(addColor, Color.parseColor("#DBEAFE"), Color.parseColor("#1D4ED8"), 6);
                addColor.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        selectedColors.add("");
                        renderRows[0].run();
                    }
                });
                colorsContainer.addView(addColor);
            }
        };
        if (input.value == null || input.value.trim().isEmpty()) {
            // No default colors are invented; the three initial rows remain empty until chosen.
            input.value = "[]";
            input.hasVerifiedDefault = false;
        }
        renderRows[0].run();
        container.addView(colorsContainer);
    }

    private interface ColorSelectionCallback {
        void onColorSelected(String hex);
    }

    private void showProviderColorPicker(String initialColor, final ColorSelectionCallback callback) {
        final LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dipToPx(18), dipToPx(12), dipToPx(18), dipToPx(8));
        final EditText hexInput = new EditText(activity);
        hexInput.setSingleLine(true);
        hexInput.setHint("#RRGGBB");
        hexInput.setText(initialColor != null ? initialColor : "");
        content.addView(hexInput);
        final View preview = new View(activity);
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dipToPx(42));
        previewParams.topMargin = dipToPx(6);
        content.addView(preview, previewParams);
        hexInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                GradientDrawable swatch = new GradientDrawable();
                swatch.setCornerRadius(dipToPx(5));
                String value = s.toString().trim();
                if (isValidHexColor(value)) swatch.setColor(Color.parseColor(value));
                else swatch.setColor(Color.TRANSPARENT);
                preview.setBackground(swatch);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        hexInput.setText(hexInput.getText().toString());
        HorizontalScrollView paletteScroll = new HorizontalScrollView(activity);
        LinearLayout palette = new LinearLayout(activity);
        palette.setOrientation(LinearLayout.HORIZONTAL);
        for (final String color : EXTENDED_COLOR_PALETTE) {
            Button swatch = new Button(activity);
            swatch.setText(color);
            swatch.setTextSize(8f);
            swatch.setTextColor(Color.parseColor(color) == Color.WHITE ? Color.BLACK : Color.WHITE);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(Color.parseColor(color));
            bg.setCornerRadius(dipToPx(5));
            swatch.setBackground(bg);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dipToPx(76), dipToPx(34));
            params.setMargins(dipToPx(2), dipToPx(5), dipToPx(2), dipToPx(2));
            palette.addView(swatch, params);
            swatch.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { hexInput.setText(color); }
            });
        }
        paletteScroll.addView(palette);
        content.addView(paletteScroll);
        new android.app.AlertDialog.Builder(activity)
                .setTitle("Choose color")
                .setView(content)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Use color", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        String value = hexInput.getText().toString().trim();
                        if (isValidHexColor(value)) callback.onColorSelected(value.toUpperCase(Locale.ROOT));
                        else logToConsole("[PROVIDER-ERR] Enter a valid HEX color before applying.");
                    }
                }).show();
    }

    private void renderMainColorListEditor(final LinearLayout container, final EditText backingInput,
                                           final String[] currentValues, final int argumentIndex,
                                           final BlockArgumentMetadata metadata) {
        final int elementFormat = getVerifiedColorListElementFormat(metadata);
        if (elementFormat == 0) return;
        final String initialValue = currentValues[argumentIndex];
        final List<String> selectedColors = readVerifiedColorList(initialValue, elementFormat);
        if (initialValue != null && !initialValue.trim().isEmpty() && !"[]".equals(initialValue.trim())) {
            try {
                convertVerifiedColorList(initialValue, metadata);
                String canonical = encodeVerifiedColorList(selectedColors, metadata);
                currentValues[argumentIndex] = canonical;
                backingInput.setText(canonical);
            } catch (Exception invalidSavedColorList) {
                logToConsole("[COLOR-LIST-ERR] Existing value was left unchanged: "
                        + invalidSavedColorList.getMessage());
            }
        }
        while (selectedColors.size() < 3) selectedColors.add("");

        TextView note = new TextView(activity);
        note.setText("Choose colors individually; selection order is preserved.");
        note.setTextSize(10f);
        note.setTextColor(Color.parseColor("#64748B"));
        note.setPadding(0, dipToPx(3), 0, dipToPx(4));
        container.addView(note);

        final LinearLayout rows = new LinearLayout(activity);
        rows.setOrientation(LinearLayout.VERTICAL);
        container.addView(rows);

        final Runnable[] renderRows = new Runnable[1];
        renderRows[0] = new Runnable() {
            @Override public void run() {
                rows.removeAllViews();
                for (int colorIndex = 0; colorIndex < selectedColors.size(); colorIndex++) {
                    final int index = colorIndex;
                    LinearLayout row = new LinearLayout(activity);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.CENTER_VERTICAL);
                    row.setPadding(0, dipToPx(3), 0, dipToPx(3));

                    View preview = new View(activity);
                    GradientDrawable previewBackground = new GradientDrawable();
                    previewBackground.setCornerRadius(dipToPx(5));
                    previewBackground.setStroke(dipToPx(1), Color.parseColor("#94A3B8"));
                    String current = selectedColors.get(index);
                    previewBackground.setColor(isValidHexColor(current)
                            ? Color.parseColor(current) : Color.TRANSPARENT);
                    preview.setBackground(previewBackground);
                    row.addView(preview, new LinearLayout.LayoutParams(dipToPx(38), dipToPx(32)));

                    TextView hexLabel = new TextView(activity);
                    hexLabel.setText(isValidHexColor(current) ? current : "Choose color " + (index + 1));
                    hexLabel.setTextColor(Color.parseColor("#334155"));
                    hexLabel.setTextSize(10f);
                    hexLabel.setPadding(dipToPx(8), 0, dipToPx(5), 0);
                    row.addView(hexLabel, new LinearLayout.LayoutParams(0,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                    Button choose = new Button(activity);
                    choose.setText("Pick");
                    applyRoundedButtonStyle(choose, Color.parseColor("#EDE9FE"),
                            Color.parseColor("#5B21B6"), 6);
                    choose.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            showProviderColorPicker(selectedColors.get(index), new ColorSelectionCallback() {
                                @Override public void onColorSelected(String hex) {
                                    selectedColors.set(index, hex);
                                    try {
                                        String encoded = encodeVerifiedColorList(selectedColors, metadata);
                                        currentValues[argumentIndex] = encoded;
                                        backingInput.setText(encoded);
                                    } catch (Exception e) {
                                        logToConsole("[COLOR-LIST-ERR] " + e.getMessage());
                                    }
                                    renderRows[0].run();
                                }
                            });
                        }
                    });
                    row.addView(choose, new LinearLayout.LayoutParams(dipToPx(66), dipToPx(38)));

                    {
                        Button remove = new Button(activity);
                        remove.setText("−");
                        applyRoundedButtonStyle(remove, Color.parseColor("#FEE2E2"),
                                Color.parseColor("#991B1B"), 6);
                        remove.setOnClickListener(new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                selectedColors.remove(index);
                                try {
                                    String encoded = encodeVerifiedColorList(selectedColors, metadata);
                                    currentValues[argumentIndex] = encoded;
                                    backingInput.setText(encoded);
                                } catch (Exception e) {
                                    logToConsole("[COLOR-LIST-ERR] " + e.getMessage());
                                }
                                renderRows[0].run();
                            }
                        });
                        row.addView(remove, new LinearLayout.LayoutParams(dipToPx(42), dipToPx(38)));
                    }
                    rows.addView(row);
                }
                Button addColor = new Button(activity);
                addColor.setText("+ Add Color");
                applyRoundedButtonStyle(addColor, Color.parseColor("#DBEAFE"),
                        Color.parseColor("#1D4ED8"), 6);
                addColor.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        selectedColors.add("");
                        renderRows[0].run();
                    }
                });
                rows.addView(addColor);
            }
        };
        renderRows[0].run();
    }

    private Map<Integer, Object> invokeSelectedProviders(Method mainMethod) throws Exception {
        Map<Integer, Object> overrides = new HashMap<Integer, Object>();
        for (ProviderSelectionSlot slot : providerSelectionSlots) {
            ProviderConfiguration configuration = selectedProviderConfigurations.get(slot.argumentIndex);
            if (configuration == null) continue;
            Method providerMethod = configuration.providerMethod;
            if (!isCurrentExtensionMethod(providerMethod)
                    || !isVerifiedProviderMethod(providerMethod)
                    || !isProviderReturnTypeCompatible(providerMethod.getReturnType(), configuration.targetType)) {
                throw new IllegalStateException("Selected provider is stale or no longer verified for this input.");
            }
            Class<?>[] parameterTypes = providerMethod.getParameterTypes();
            Object[] arguments = new Object[parameterTypes.length];
            int offset = parameterTypes.length > 0 && (AndroidViewComponent.class.isAssignableFrom(parameterTypes[0])
                    || View.class.isAssignableFrom(parameterTypes[0])) ? 1 : 0;
            if (offset == 1) arguments[0] = getSelectedTargetView(parameterTypes[0]);
            for (int i = 0; i < configuration.inputs.size(); i++) {
                ProviderArgumentInput input = configuration.inputs.get(i);
                if (!input.hasVerifiedDefault && input.value.trim().isEmpty()) {
                    throw new IllegalArgumentException("No verified default for provider input " + input.label
                            + "; enter a value before execution.");
                }
                if (YailList.class.isAssignableFrom(input.javaType)
                        && isDeclaredColorListMetadata(input.metadata)) {
                    if (getVerifiedColorListElementFormat(input.metadata) == 0) {
                        throw new IllegalArgumentException("Color-list metadata for " + input.label
                                + " is incomplete or unsupported; provider execution was stopped.");
                    }
                    arguments[input.parameterIndex] = convertVerifiedColorList(input);
                } else {
                    arguments[input.parameterIndex] = convertArgument(input.value, input.javaType);
                }
            }
            if (!isCurrentExtensionMethod(providerMethod) || !isCurrentExtensionMethod(mainMethod)) {
                throw new IllegalStateException("DEX changed before provider invocation; select the methods again.");
            }
            final Object providerResult;
            try {
                providerResult = providerMethod.invoke(currentExtensionInstance, arguments);
            } catch (java.lang.reflect.InvocationTargetException e) {
                Throwable cause = e.getTargetException() != null ? e.getTargetException() : e;
                throw new IllegalStateException("Provider " + formatMethodIdentity(providerMethod) + " failed: "
                        + (cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName()), cause);
            }
            if (providerResult == null) {
                throw new IllegalArgumentException("Provider " + formatMethodIdentity(providerMethod)
                        + " returned null; nullable input behavior is not verified by the available metadata.");
            }
            Object converted = convertRuntimeArgument(providerResult, configuration.targetType);
            overrides.put(configuration.mainArgumentIndex, converted);
            logToConsole("[PROVIDER] " + formatMethodIdentity(providerMethod)
                    + " returned a value for argument " + (configuration.mainArgumentIndex + 1) + ".");
        }
        return overrides;
    }

    private void scheduleProviderPreviewIfReady(final Method mainMethod) {
        if (mainMethod == null || selectedProviderConfigurations.isEmpty() || activity == null) return;
        if (pendingProviderPreviewRunnable != null) {
            mainHandler.removeCallbacks(pendingProviderPreviewRunnable);
            pendingProviderPreviewRunnable = null;
        }
        for (ProviderConfiguration configuration : selectedProviderConfigurations.values()) {
            for (ProviderArgumentInput input : configuration.inputs) {
                if (input.value == null || input.value.trim().isEmpty()) return;
                try {
                    if (YailList.class.isAssignableFrom(input.javaType)
                            && isDeclaredColorListMetadata(input.metadata)) {
                        if (getVerifiedColorListElementFormat(input.metadata) == 0) return;
                        convertVerifiedColorList(input);
                    } else {
                        convertArgument(input.value, input.javaType);
                    }
                } catch (Exception incompleteInput) { return; }
            }
        }
        pendingProviderPreviewRunnable = new Runnable() {
            @Override public void run() {
                pendingProviderPreviewRunnable = null;
                if (getSelectedMethodEntry() != mainMethod || selectedProviderConfigurations.isEmpty()) return;
                executeSelectedMethodWithProvidersFromUI();
            }
        };
        mainHandler.postDelayed(pendingProviderPreviewRunnable, 500L);
    }

    private boolean hasVerifiedDefaultsForMethod(Method method) {
        if (method == null) return false;
        Class<?>[] types = method.getParameterTypes();
        int offset = types.length > 0 && (AndroidViewComponent.class.isAssignableFrom(types[0])
                || View.class.isAssignableFrom(types[0])) ? 1 : 0;
        for (int i = offset; i < types.length; i++) {
            BlockArgumentMetadata metadata = getBlockArgumentMetadata(method, i);
            if (metadata == null || metadata.defaultValue == null || metadata.defaultValue.trim().isEmpty()) return false;
            try { convertArgument(metadata.defaultValue, types[i]); }
            catch (Exception invalidDefault) { return false; }
        }
        return true;
    }

    private void initializeRestoredMethodViewIfVerified(Method method) {
        if (method == null || !hasVerifiedDefaultsForMethod(method)) {
            logToConsole("[AUTO-RENDER] Skipped: the selected method has no complete, verified default arguments.");
            return;
        }
        Class<?>[] types = method.getParameterTypes();
        int offset = types.length > 0 && (AndroidViewComponent.class.isAssignableFrom(types[0])
                || View.class.isAssignableFrom(types[0])) ? 1 : 0;
        for (int i = offset; i < types.length; i++) {
            if (selectedGetterProperties.containsKey(getPropertySelectionKey(method, i - offset))) return;
        }
        logToConsole("[AUTO-RENDER] Initializing the previously selected method from verified defaults.");
        autoRenderPendingMethodIdentity = "";
        scheduleRenderPipeline(method, generateTypedBlockDefaults(method));
    }

    private void executeSelectedMethodWithProvidersFromUI() {
        if (selectedProviderConfigurations.isEmpty()) {
            executeSelectedMethodFromUI();
            return;
        }
        if (currentExtensionClass == null || methodSpinner == null) return;
        final Method targetMethod = getSelectedMethodEntry();
        if (targetMethod == null || !validateSelectedPropertySources(targetMethod)) return;
        if (!providerSelectionMainMethod.equals(lastDexChecksum + "|" + formatMethodIdentity(targetMethod))) {
            logToConsole("[PROVIDER-ERR] Provider mapping is stale; select it again for the current method.");
            return;
        }
        final List<String> arguments;
        try {
            arguments = parseArgumentTokens(argsEditText != null ? argsEditText.getText().toString().trim() : "");
        } catch (IllegalArgumentException e) {
            logToConsole("[INPUT-ERR] " + e.getMessage());
            return;
        }
        Class<?>[] parameterTypes = targetMethod.getParameterTypes();
        int expected = parameterTypes.length;
        if (expected > 0 && (AndroidViewComponent.class.isAssignableFrom(parameterTypes[0])
                || View.class.isAssignableFrom(parameterTypes[0]))) expected--;
        if (arguments.size() != expected) {
            logToConsole("[INPUT-ERR] Expected " + expected + " main argument(s), found " + arguments.size() + ".");
            return;
        }
        for (Integer index : selectedProviderConfigurations.keySet()) {
            if (selectedGetterProperties.containsKey(getPropertySelectionKey(targetMethod, index))) {
                logToConsole("[PROVIDER-ERR] This input already uses a Getter value. Choose direct input or clear the Getter selection first.");
                return;
            }
        }
        if (!validateMethodTarget(targetMethod)) return;
        View target = getSelectedTargetNativeView();
        capturePristineBaseline(target);
        if (!isInsideListHierarchy(target)) applyCenterTargetLayout(target);
        String cacheKey = getMethodArgumentsCacheKey(targetMethod);
        try {
            activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
                    .putString(cacheKey, argsEditText != null ? argsEditText.getText().toString().trim() : "").apply();
        } catch (Exception ignored) {}
        scheduleProviderRenderPipeline(targetMethod, argsEditText != null ? argsEditText.getText().toString().trim() : "");
    }

    private void scheduleProviderRenderPipeline(final Method targetMethod, final String args) {
        final long generation = globalRenderGeneration.incrementAndGet();
        if (pendingRenderRunnable != null) {
            mainHandler.removeCallbacks(pendingRenderRunnable);
            pendingRenderRunnable = null;
        }
        if (!isInsideListHierarchy(getSelectedTargetNativeView())) stopKnownExtensionAnimations();
        pendingRenderRunnable = new Runnable() {
            @Override public void run() {
                if (generation != globalRenderGeneration.get()) return;
                pendingRenderRunnable = null;
                activity.runOnUiThread(new Runnable() {
                    @Override public void run() {
                        try {
                            if (generation != globalRenderGeneration.get() || !validateMethodTarget(targetMethod)) return;
                            Map<Integer, Object> overrides = invokeSelectedProviders(targetMethod);
                            runMethodWithArgs(targetMethod, targetMethod.getParameterTypes(), args, overrides);
                        } catch (Throwable t) {
                            recordSavedProjectTest(targetMethod.getName(), args, "FAILURE", getProviderHistorySnapshot(), null,
                                    t.getClass().getSimpleName() + ": " + t.getMessage());
                            inspectAndLogExtensionError(t, targetMethod.getName(), args);
                        }
                    }
                });
            }
        };
        mainHandler.postDelayed(pendingRenderRunnable, RENDER_FLUSH_DELAY_MS);
    }

    private boolean isIntegerType(Class<?> type) {
        return type == byte.class || type == Byte.class || type == short.class || type == Short.class
                || type == int.class || type == Integer.class || type == long.class || type == Long.class;
    }

    private void applyRoundedButtonStyle(Button btn, int bgColor, int textColor, int radiusDp) {
        if (btn == null) return;
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(bgColor);
        gd.setCornerRadius(dipToPx(radiusDp));
        btn.setBackground(gd);
        btn.setTextColor(textColor);
        btn.setPadding(dipToPx(8), dipToPx(4), dipToPx(8), dipToPx(4));
        btn.setTextSize(11f);
        btn.setMinHeight(dipToPx(32));
        btn.setMinimumHeight(dipToPx(32));
        btn.setTransformationMethod(null);
        applyUiElevation(btn, 2);
    }

    private void applyCompactSelectorStyle(Spinner spinner) {
        if (spinner == null) return;
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            spinner.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#64748B")));
        }
        spinner.setPadding(dipToPx(8), 0, dipToPx(8), 0);
        spinner.setMinimumHeight(dipToPx(36));
        spinner.setDropDownVerticalOffset(dipToPx(2));
    }

    private void applyUiElevation(View view, int elevationDp) {
        if (view != null && android.os.Build.VERSION.SDK_INT >= 21) {
            view.setElevation(dipToPx(elevationDp));
        }
    }

    private String getLocalizedText(String key) {
        switch (key) {
            case "minimize":
                return tx("button.minimize");
            case "expand":
                return tx("button.expand");
            case "execute":
                return "\u25B6  " + tx("button.execute");
            case "reset":
                return "\u21BA  " + tx("button.reset");
            case "tune":
                return "\uD83C\uDFA8  " + tx("button.tune") + "  >";
            case "logs_title":
                return tx("logs.title");
            case "clear":
                return tx("button.clear");
            case "hide_logs":
                return tx("button.hideLogs");
            case "show_logs":
                return tx("button.showLogs");
            case "param_guide":
                return tx("parameter.guide");
            default:
                return "";
        }
    }

    private String tx(String key, Object... args) {
        return Localization.get(currentLanguageCode, key, args);
    }

    private String localizeMessage(String message) {
        if (message == null) return null;
        if ("PASS".equals(message)) return tx("status.pass");
        if ("WARNING".equals(message)) return tx("status.warning");
        if ("ERROR".equals(message)) return tx("status.error");
        if ("NOT TESTED".equals(message)) return tx("status.notTested");
        String translated = Localization.translate(currentLanguageCode, message);
        if (translated != message && !translated.equals(message)) return translated;
        int prefixEnd = message.indexOf("] ");
        if (prefixEnd >= 0) {
            String prefix = message.substring(0, prefixEnd + 2);
            String body = message.substring(prefixEnd + 2);
            String localizedBody = Localization.translate(currentLanguageCode, body);
            if (!localizedBody.equals(body)) return prefix + localizedBody;
        }
        for (String status : new String[]{"NOT TESTED", "WARNING", "ERROR", "PASS"}) {
            if (message.startsWith(status + " ") || message.startsWith(status + " —") || message.startsWith(status + " (")) {
                return localizeMessage(status) + message.substring(status.length());
            }
        }
        return message;
    }

    private String formatCardHost(String host) {
        if (host == null || host.isEmpty()) return tx("pc.notConnected");
        if ("Searching...".equals(host)) return tx("connection.searchingStatus");
        if ("Not Found".equals(host)) return tx("connection.notFound");
        return tx("pc.ip", host);
    }

    private boolean handleBlockMetadataMessage(String message) {
        final JSONObject envelope;
        try {
            envelope = new JSONObject(message);
        } catch (Exception ignored) {
            return false;
        }
        if (!"bolt.parameter-metadata".equals(envelope.optString("type"))) return false;

        if (envelope.optInt("version", -1) != 1) {
            logToConsole("[METADATA-ERR] Unsupported parameter metadata version.");
            return true;
        }
        String dexMd5 = envelope.optString("dexMd5", "");
        String aixPath = envelope.optString("aixPath", "<unspecified>");
        if (dexMd5.isEmpty() || !dexMd5.equals(lastDexChecksum)) {
            logToConsole("[METADATA-ERR] AIX=" + aixPath + "; DEX checksum mismatch: metadata="
                    + dexMd5 + ", received=" + lastDexChecksum + ".");
            return true;
        }
        lastMetadataDexChecksum = dexMd5;
        if (currentExtensionClass == null || currentClassName == null) {
            logToConsole("[METADATA-ERR] Load the extension before applying block metadata.");
            return true;
        }

        JSONArray components = envelope.optJSONArray("components");
        if (components == null) {
            logToConsole("[METADATA-ERR] Block metadata has no components array.");
            return true;
        }

        JSONObject matchingComponent = null;
        int matchingComponentCount = 0;
        StringBuilder componentTypes = new StringBuilder();
        for (int i = 0; i < components.length(); i++) {
            JSONObject component = components.optJSONObject(i);
            if (component != null) {
                if (componentTypes.length() > 0) componentTypes.append(", ");
                componentTypes.append(component.optString("type", "<missing type>"));
            }
            if (component != null && currentClassName.equals(component.optString("type"))) {
                matchingComponent = component;
                matchingComponentCount++;
            }
        }
        if (matchingComponentCount != 1) {
            blockPropertyMetadata.clear();
            blockPropertyMetadataDexChecksum = "";
            logToConsole("[METADATA] AIX=" + aixPath + "; DEX=" + dexMd5 + ", loaded class=" + currentClassName
                    + ", metadata component types=[" + componentTypes + "]; "
                    + (matchingComponentCount == 0 ? "no class match." : "ambiguous class match count=" + matchingComponentCount + "."));
            return true;
        }

        lastMetadataEnvelope = envelope.toString();

        blockPropertyMetadata.clear();
        selectedGetterProperties.clear();
        JSONArray metadataProperties = matchingComponent.optJSONArray("blockProperties");
        if (metadataProperties != null) {
            for (int i = 0; i < metadataProperties.length(); i++) {
                JSONObject property = metadataProperties.optJSONObject(i);
                if (property == null) continue;
                String propertyName = property.optString("name", "").trim();
                String propertyType = property.optString("type", "").trim();
                String access = property.optString("rw", "").trim().toLowerCase(Locale.ROOT);
                if (!propertyName.isEmpty() && !propertyType.isEmpty()
                        && ("read-only".equals(access) || "read-write".equals(access)
                        || "write-only".equals(access))) {
                    blockPropertyMetadata.add(new BlockPropertyMetadata(propertyName, propertyType, access));
                }
            }
        }
        // If the AIX has no blockProperties array, discover only conservative JavaBean
        // getters from the checksum-matched, currently loaded extension class. These are
        // read-only sources; arbitrary methods and Android lifecycle methods are excluded.
        if (metadataProperties == null || metadataProperties.length() == 0) {
            if (currentExtensionClass != null && currentExtensionInstance != null
                    && currentExtensionClass.isInstance(currentExtensionInstance)) {
                for (Method candidate : currentExtensionClass.getMethods()) {
                    if (!isVerifiedGetterCandidate(candidate)) continue;
                    String methodName = candidate.getName();
                    Class<?> rt = candidate.getReturnType();
                    String inferredType = inferGetterBlockType(rt);
                    if (inferredType == null) continue;
                    boolean duplicate = false;
                    for (BlockPropertyMetadata existing : blockPropertyMetadata) {
                        if (existing.name.equals(methodName)) { duplicate = true; break; }
                    }
                    if (!duplicate) {
                        blockPropertyMetadata.add(new BlockPropertyMetadata(methodName, inferredType, "read-only"));
                    }
                }
                logToConsole("[GETTER-DISCOVERY] Discovered " + blockPropertyMetadata.size()
                        + " conservative read-only JavaBean getter(s) from the current DEX class.");
            }
        }
        blockPropertyMetadataDexChecksum = dexMd5;
        int readableProperties = 0;
        int writableProperties = 0;
        for (BlockPropertyMetadata property : blockPropertyMetadata) {
            if (findPropertyAccessor(property, true) != null) readableProperties++;
            if (findPropertyAccessor(property, false) != null) writableProperties++;
        }
        logToConsole("[PROPERTY-METADATA] AIX=" + aixPath + "; DEX=" + dexMd5
                + "; parsed blockProperties=" + blockPropertyMetadata.size()
                + "; matched getters=" + readableProperties
                + "; matched setters=" + writableProperties + ".");

        JSONArray metadataMethods = matchingComponent.optJSONArray("methods");
        if (metadataMethods == null) {
            logToConsole("[METADATA] Matching component has no methods metadata; block properties remain available.");
            return true;
        }

        Map<String, Integer> metadataMethodCounts = new HashMap<String, Integer>();
        for (int i = 0; i < metadataMethods.length(); i++) {
            JSONObject entry = metadataMethods.optJSONObject(i);
            if (entry == null) continue;
            JSONArray params = entry.optJSONArray("params");
            String key = entry.optString("name") + "#" + (params != null ? params.length() : -1);
            Integer count = metadataMethodCounts.get(key);
            metadataMethodCounts.put(key, count == null ? 1 : count + 1);
        }

        int matchedMethods = 0;
        for (int i = 0; i < metadataMethods.length(); i++) {
            JSONObject entry = metadataMethods.optJSONObject(i);
            if (entry == null) continue;
            String methodName = entry.optString("name", "");
            JSONArray params = entry.optJSONArray("params");
            if (methodName.isEmpty() || params == null) continue;
            Integer metadataCount = metadataMethodCounts.get(methodName + "#" + params.length());
            if (metadataCount == null || metadataCount != 1) continue;

            List<String> names = new ArrayList<String>();
            List<String> blockTypes = new ArrayList<String>();
            List<BlockArgumentMetadata> argumentMetadata = new ArrayList<BlockArgumentMetadata>();
            boolean validMetadataParams = true;
            for (int parameterIndex = 0; parameterIndex < params.length(); parameterIndex++) {
                JSONObject parameter = params.optJSONObject(parameterIndex);
                if (parameter == null) {
                    validMetadataParams = false;
                    break;
                }
                String parameterName = parameter.optString("name", "").trim();
                String parameterType = parameter.optString("type", "").trim();
                String semanticBlockType = parameter.optString("blockType", parameterType).trim();
                if (parameterName.isEmpty() || parameterType.isEmpty()) {
                    validMetadataParams = false;
                    break;
                }
                names.add(parameterName);
                blockTypes.add(parameterType);
                argumentMetadata.add(new BlockArgumentMetadata(parameterName, semanticBlockType,
                        readOptionalMetadataValue(parameter, "defaultValue"),
                        firstOptionalMetadataValue(parameter, "elementType", "itemType"),
                        firstOptionalMetadataValue(parameter, "elementFormat", "itemFormat"),
                        readOptionalMetadataValue(parameter, "minValue"),
                        readOptionalMetadataValue(parameter, "maxValue")));
            }
            if (!validMetadataParams) continue;

            List<Method> compatibleCandidates = new ArrayList<Method>();
            for (Method candidate : currentExtensionClass.getMethods()) {
                if (candidate.getDeclaringClass() != currentExtensionClass
                        || candidate.isBridge() || candidate.isSynthetic()
                        || !candidate.getName().equals(methodName)
                        || candidate.getParameterTypes().length != params.length()) continue;
                Class<?>[] javaTypes = candidate.getParameterTypes();
                boolean compatible = true;
                for (int parameterIndex = 0; parameterIndex < javaTypes.length; parameterIndex++) {
                    if (!isBlockParameterTypeCompatible(blockTypes.get(parameterIndex), javaTypes[parameterIndex])) {
                        compatible = false;
                        break;
                    }
                }
                if (compatible) compatibleCandidates.add(candidate);
            }
            if (compatibleCandidates.size() != 1) continue;

            Method matchedMethod = compatibleCandidates.get(0);
            blockParameterNamesByMethod.put(formatMethodIdentity(matchedMethod), names);
            blockArgumentMetadataByMethod.put(formatMethodIdentity(matchedMethod), argumentMetadata);
            matchedMethods++;
        }

if (methodAdapter != null) methodAdapter.notifyDataSetChanged();
        Method selectedMethod = getSelectedMethodEntry();
        if (selectedMethod != null) {
            updateParamInfo(selectedMethod.getName());
            if (!autoRenderPendingMethodIdentity.isEmpty()
                    && autoRenderPendingMethodIdentity.equals(formatMethodIdentity(selectedMethod))) {
                initializeRestoredMethodViewIfVerified(selectedMethod);
            }
        }
        logToConsole("[METADATA] Verified block parameter names for " + matchedMethods + " method(s).");
        return true;
    }

    private Class<?> normalizeBoxedType(Class<?> c) {
        if (c == Integer.class) return int.class;
        if (c == Float.class) return float.class;
        if (c == Double.class) return double.class;
        if (c == Long.class) return long.class;
        if (c == Short.class) return short.class;
        if (c == Byte.class) return byte.class;
        if (c == Boolean.class) return boolean.class;
        if (c == Character.class) return char.class;
        return c;
    }

    private boolean isJavaTypeStrictlyCompatible(Class<?> getterType, Class<?> paramType) {
        if (getterType == null || paramType == null) return false;
        Class<?> g = normalizeBoxedType(getterType);
        Class<?> p = normalizeBoxedType(paramType);
        if (g == p) return true;
        if (g == String.class && p == String.class) return true;
        if (g == boolean.class && p == boolean.class) return true;
        return false;
    }

    private boolean isBlockParameterTypeCompatible(String blockType, Class<?> javaType) {
        String type = blockType != null ? blockType.trim().toLowerCase(Locale.ROOT) : "";
        if ("text".equals(type)) return javaType == String.class;
        if ("boolean".equals(type)) return javaType == boolean.class || javaType == Boolean.class;
        if ("number".equals(type)) {
            return javaType == byte.class || javaType == Byte.class
                    || javaType == short.class || javaType == Short.class
                    || javaType == int.class || javaType == Integer.class
                    || javaType == long.class || javaType == Long.class
                    || javaType == float.class || javaType == Float.class
                    || javaType == double.class || javaType == Double.class;
        }
        if ("list".equals(type)) {
            return YailList.class.isAssignableFrom(javaType)
                    || List.class.isAssignableFrom(javaType);
        }
        if ("component".equals(type)) {
            return Component.class.isAssignableFrom(javaType)
                    || AndroidViewComponent.class.isAssignableFrom(javaType);
        }
        if ("any".equals(type)) return javaType == Object.class;
        return blockType != null && blockType.equals(javaType.getName());
    }

    private void applyLanguageChange() {
        if (runButton != null) runButton.setText(getLocalizedText("execute"));
        if (resetButton != null) resetButton.setText(getLocalizedText("reset"));
        if (minimizeButton != null) minimizeButton.setText(isPanelMinimized ? getLocalizedText("expand") : getLocalizedText("minimize"));
        if (openTuningTile != null && openTuningTile.getChildAt(0) instanceof TextView) {
            ((TextView) openTuningTile.getChildAt(0)).setText(getLocalizedText("tune"));
        }
        if (panelTitleView != null) panelTitleView.setText(tx("panel.title"));
        if (cardMethodNameView != null) cardMethodNameView.setText(localizeMessage(lastCardMethodName));
        if (cardTitleView != null) cardTitleView.setText(tx("app.title"));
        if (cardIpView != null) cardIpView.setText("\uD83D\uDFE2 " + formatCardHost(lastCardHostIp));
        if (cardStatusView != null) cardStatusView.setText(localizeMessage(lastCardStatusMessage));
        if (cardFindPcButton != null) cardFindPcButton.setText("\uD83D\uDCF6  " + tx("button.findPc"));
        if (rescanTargetsButton != null) rescanTargetsButton.setContentDescription(tx("button.rescan"));
        if (languageSpinner != null) languageSpinner.setContentDescription(tx("language.title"));
        if (consoleTitle != null) consoleTitle.setText(getLocalizedText("logs_title"));
        if (clearLogsButton != null) clearLogsButton.setText(getLocalizedText("clear"));
        if (toggleLogsButton != null) toggleLogsButton.setText(isConsoleVisible ? getLocalizedText("hide_logs") : getLocalizedText("show_logs"));
        if (paramInfoLabel != null) {
            String currentParamLabel = paramInfoLabel.getText() != null ? paramInfoLabel.getText().toString() : "";
            String translatedParamLabel = Localization.translateFromAnyLanguage(currentLanguageCode, currentParamLabel);
            if (!translatedParamLabel.equals(currentParamLabel)) {
                paramInfoLabel.setText(translatedParamLabel);
            } else {
                boolean matchedPrefix = false;
                for (String languageCode : Localization.getLanguageCodes()) {
                    String oldPrefix = Localization.get(languageCode, "guide.parametersLabel");
                    if (currentParamLabel.startsWith(oldPrefix)) {
                        paramInfoLabel.setText(tx("guide.parametersLabel") + currentParamLabel.substring(oldPrefix.length()));
                        matchedPrefix = true;
                        break;
                    }
                }
                if (!matchedPrefix && (argsEditText == null || argsEditText.getText().toString().isEmpty())) {
                    paramInfoLabel.setText(getLocalizedText("param_guide"));
                }
            }
        }
        if (argsEditText != null && argsEditText.getHint() != null) {
            String oldHint = argsEditText.getHint().toString();
            String translatedHint = Localization.translateFromAnyLanguage(currentLanguageCode, oldHint);
            if (!translatedHint.equals(oldHint)) argsEditText.setHint(translatedHint);
        }
        try {
            SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(PREF_LANG_KEY, currentLanguageCode).apply();
        } catch (Exception ignored) {}
    }

    private int[] countExtensionBlocks() {
        int methods = 0, getters = 0, setters = 0, events = 0;
        if (currentExtensionClass == null) return new int[]{0, 0, 0, 0};

        ensureDiscoveredGetterMetadata();
        java.util.Set<String> verifiedSetterIdentities = new java.util.HashSet<String>();

        for (BlockPropertyMetadata property : blockPropertyMetadata) {
            if (("read-write".equals(property.access) || "write-only".equals(property.access))) {
                Method setter = findPropertyAccessor(property, false);
                if (setter != null) {
                    verifiedSetterIdentities.add(formatMethodIdentity(setter));
                }
            }
        }

        for (Method m : currentExtensionClass.getMethods()) {
            if (m.getDeclaringClass() == Object.class || m.isBridge() || m.isSynthetic()) continue;
            Class<?>[] p = m.getParameterTypes();
            String identity = formatMethodIdentity(m);

            if (verifiedSetterIdentities.contains(identity)) {
                setters++;
            } else if (p.length == 0 && m.getReturnType() != void.class) {
                getters++;
            } else if (p.length > 0) {
                methods++;
            }
        }

        for (Class<?> inner : currentExtensionClass.getDeclaredClasses()) {
            if (inner.isInterface() && (inner.getName().contains("Listener") || inner.getName().contains("Callback") || inner.getName().contains("Event"))) {
                events += inner.getMethods().length;
            }
        }
        if (events == 0) {
            for (Method m : currentExtensionClass.getMethods()) {
                if (m.getName().startsWith("On") || m.getName().startsWith("on")) {
                    events++;
                }
            }
        }
        return new int[]{methods, getters, setters, events};
    }

    private String[] getGuideNetworkState() {
        try {
            ConnectivityManager manager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo network = manager != null ? manager.getActiveNetworkInfo() : null;
            if (network == null) return new String[]{"NOT TESTED", tx("guide.diag.networkUnavailable")};
            if (network.isConnected()) return new String[]{"PASS", tx("guide.diag.networkConnected")};
            return new String[]{"WARNING", tx("guide.diag.networkDisconnected")};
        } catch (Exception e) {
            return new String[]{"NOT TESTED", tx("guide.diag.networkUnreadable")};
        }
    }

    private Method getGuideSelectedMethod() {
        if (currentExtensionClass == null) return null;
        Method selected = getSelectedMethodEntry();
        if (selected != null) return selected;
        if (userSelectedMethod == null || userSelectedMethod.trim().isEmpty()) return null;
        String args = argsEditText != null ? argsEditText.getText().toString().trim() : "";
        try {
            return findMethodByNameAndArgs(userSelectedMethod, parseArgumentTokens(args).size());
        } catch (IllegalArgumentException e) {
            lastMethodResolutionError = e.getMessage();
            return null;
        }
    }

    private String[] getGuideParameterState(Method method) {
        if (method == null) return new String[]{"NOT TESTED", tx("guide.diag.chooseMethodParams")};
        Class<?>[] types = method.getParameterTypes();
        int offset = types.length > 0 && (AndroidViewComponent.class.isAssignableFrom(types[0])
                || View.class.isAssignableFrom(types[0])) ? 1 : 0;
        int expectedCount = types.length - offset;
        String rawArgs = argsEditText != null ? argsEditText.getText().toString().trim() : "";
        List<String> values;
        try {
            values = parseArgumentTokens(rawArgs);
        } catch (IllegalArgumentException e) {
            return new String[]{"ERROR", e.getMessage()};
        }
        if (values.size() != expectedCount) {
            return new String[]{"WARNING", tx("guide.diag.argumentCount", expectedCount, values.size())};
        }

        for (int i = 0; i < expectedCount; i++) {
            Class<?> type = types[i + offset];
            try {
                convertArgument(values.get(i), type);
            } catch (Exception e) {
                return new String[]{"ERROR", tx("guide.diag.parameterError", i + 1, localizeMessage(e.getMessage()))};
            }
        }
        return new String[]{"PASS", expectedCount == 0 ? tx("guide.diag.noUserParams") : tx("guide.diag.allArgsMatch")};
    }

    private String[] getGuideTargetState(Method method) {
        View target = getSelectedTargetNativeView();
        if (target == null) return new String[]{"WARNING", tx("guide.diag.targetViewMissing")};
        Class<?>[] types = method != null ? method.getParameterTypes() : new Class<?>[0];
        if (types.length == 0) return new String[]{"PASS", tx("guide.diag.targetNoParam")};
        Class<?> expected = types[0];
        if (View.class.isAssignableFrom(expected)) {
            if (expected.isInstance(target)) return new String[]{"PASS", tx("guide.diag.viewMatches", expected.getSimpleName())};
            return new String[]{"ERROR", tx("guide.diag.viewMismatch", target.getClass().getSimpleName(), expected.getSimpleName())};
        }
        if (expected == AndroidViewComponent.class) {
            return new String[]{"PASS", tx("guide.diag.componentAccepted", "AndroidViewComponent", target.getClass().getName())};
        }
        if (expected == Component.class) {
            return new String[]{"PASS", tx("guide.diag.componentAccepted", "Component", target.getClass().getName())};
        }
        if (AndroidViewComponent.class.isAssignableFrom(expected) || Component.class.isAssignableFrom(expected)) {
            return new String[]{"WARNING", tx("target.componentWarning")};
        }
        return new String[]{"PASS", tx("guide.diag.targetSelected")};
    }

    private void addGuideSectionTitle(LinearLayout parent, String text) {
        TextView title = new TextView(activity);
        title.setText(localizeMessage(text));
        title.setTextSize(12f);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.parseColor("#0F172A"));
        title.setPadding(0, dipToPx(10), 0, dipToPx(4));
        parent.addView(title);
    }

    private void addGuideStatusRow(LinearLayout parent, String label, String status, String detail) {
        int color;
        String icon;
        if ("PASS".equals(status)) {
            color = Color.parseColor("#047857");
            icon = "\u2713";
        } else if ("ERROR".equals(status)) {
            color = Color.parseColor("#B91C1C");
            icon = "\u2715";
        } else if ("WARNING".equals(status)) {
            color = Color.parseColor("#B45309");
            icon = "\u26A0";
        } else {
            color = Color.parseColor("#64748B");
            icon = "\u25CB";
        }
        TextView row = new TextView(activity);
        String localizedStatus = "PASS".equals(status) ? tx("status.pass")
                : ("WARNING".equals(status) ? tx("status.warning")
                : ("ERROR".equals(status) ? tx("status.error") : tx("status.notTested")));
        row.setText(icon + " " + localizedStatus + "  " + localizeMessage(label)
                + (detail == null || detail.isEmpty() ? "" : "\n     " + localizeMessage(detail)));
        row.setTextSize(10.5f);
        row.setTextColor(color);
        row.setPadding(dipToPx(8), dipToPx(5), dipToPx(8), dipToPx(5));
        row.setBackgroundColor(Color.parseColor("#F8FAFC"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dipToPx(3);
        parent.addView(row, lp);
    }

    private void addGuideProblem(LinearLayout parent, String problem, String cause, String solution, boolean ready) {
        TextView card = new TextView(activity);
        if (ready) {
            card.setText("\u2713 " + tx("guide.ready"));
            card.setTextColor(Color.parseColor("#047857"));
            card.setBackgroundColor(Color.parseColor("#ECFDF5"));
        } else {
            card.setText(tx("guide.problem", localizeMessage(problem), localizeMessage(cause), localizeMessage(solution)));
            card.setTextColor(Color.parseColor("#334155"));
            card.setBackgroundColor(Color.parseColor("#FFF7ED"));
        }
        card.setTextSize(11f);
        card.setPadding(dipToPx(9), dipToPx(7), dipToPx(9), dipToPx(7));
        parent.addView(card);
    }

    private void renderGuideDiagnostics(final LinearLayout diagnostics, final boolean[] technicalExpanded) {
        try {
            diagnostics.removeAllViews();
            addGuideSectionTitle(diagnostics, tx("guide.currentStatusHeading"));
            Button recheck = new Button(activity);
            recheck.setText("↻ " + tx("guide.recheck"));
            applyRoundedButtonStyle(recheck, Color.parseColor("#DBEAFE"), Color.parseColor("#1D4ED8"), 10);
            recheck.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    renderGuideDiagnostics(diagnostics, technicalExpanded);
                }
            });
            diagnostics.addView(recheck);

            String[] network = getGuideNetworkState();
            Socket observedSocket = socket;
            boolean liveSocket = false;
            String socketStatus;
            String socketDetail;
            try {
                liveSocket = observedSocket != null && observedSocket.isConnected() && !observedSocket.isClosed();
                socketStatus = liveSocket ? "PASS"
                        : (observedSocket == null && lastConnectedHost.isEmpty() ? "NOT TESTED"
                        : (observedSocket == null || observedSocket.isClosed() ? "ERROR" : "WARNING"));
                socketDetail = observedSocket == null ? tx("guide.diag.socketMissing")
                        : (liveSocket ? tx("guide.diag.socketOpen")
                        : (observedSocket.isClosed() ? tx("guide.diag.socketClosed") : tx("guide.diag.socketDisconnected")));
            } catch (Exception e) {
                socketStatus = "NOT TESTED";
                socketDetail = tx("guide.diag.socketUnreadable");
            }
            String connectionStatus;
            String connectionDetail;
            if (isConnected && liveSocket) {
                connectionStatus = "PASS";
                connectionDetail = tx("guide.diag.websocketActive");
            } else if (isConnected || !lastConnectedHost.isEmpty()) {
                connectionStatus = "ERROR";
                connectionDetail = tx("guide.diag.noWebsocket");
            } else {
                connectionStatus = "NOT TESTED";
                connectionDetail = tx("guide.diag.noAttempt");
            }
            String dexStatus = lastBinaryData != null && lastBinaryData.length > 0 ? "PASS" : "NOT TESTED";
            String dexDetail = dexStatus.equals("PASS") ? tx("guide.diag.dexBytes", lastBinaryData.length) : tx("guide.diag.noDex");
            String extensionStatus = lastExtensionLoadStatus;
            String extensionDetail = lastExtensionLoadMessage;
            if ("PASS".equals(lastExtensionLoadStatus) && currentExtensionClass != null && currentExtensionInstance != null) {
                extensionStatus = "PASS";
                extensionDetail = currentClassName;
            }
            String constructorStatus = currentExtensionInstance != null ? "PASS"
                    : (currentExtensionClass != null ? "ERROR" : "NOT TESTED");
            String constructorDetail = currentExtensionInstance != null
                    ? tx("guide.diag.instanceCreated", lastConstructorSignature.isEmpty() ? tx("guide.diag.signatureUnavailable") : lastConstructorSignature)
                    : (currentExtensionClass != null ? tx("guide.diag.classNoInstance") : tx("guide.diag.noClass"));

            Method selectedMethod = getGuideSelectedMethod();
            String selectedMethodName = methodSpinner != null && methodSpinner.getSelectedItem() != null
                    ? String.valueOf(methodSpinner.getSelectedItem()) : userSelectedMethod;
            String methodStatus = selectedMethod != null ? "PASS"
                    : (selectedMethodName == null || selectedMethodName.trim().isEmpty() ? "NOT TESTED" : "ERROR");
            String methodDetail = selectedMethod != null ? tx("guide.diag.methodAvailable", selectedMethod.getName())
                    : (methodStatus.equals("ERROR") ? tx("guide.diag.methodUnresolved") : tx("guide.diag.noMethod"));
            String[] target = getGuideTargetState(selectedMethod);
            String[] parameters = getGuideParameterState(selectedMethod);
            String executionStatus = lastExecutionStatus;
            String executionDetail = lastExecutionMessage.isEmpty() ? tx("guide.diag.noExecution") : lastExecutionMessage;

            addGuideStatusRow(diagnostics, tx("guide.networkLabel"), network[0], network[1]);
            addGuideStatusRow(diagnostics, tx("guide.pcWebsocket"), connectionStatus, connectionDetail);
            addGuideStatusRow(diagnostics, tx("guide.socket"), socketStatus, socketDetail);
            addGuideStatusRow(diagnostics, tx("guide.discovery"), lastDiscoveryStatus, lastDiscoveryMessage);
            addGuideStatusRow(diagnostics, tx("guide.dexReceived"), dexStatus, dexDetail);
            addGuideStatusRow(diagnostics, tx("guide.dexVerification"), lastDexVerificationStatus,
                    lastDexVerificationMessage.isEmpty() ? tx("dex.verifyNotTested") : lastDexVerificationMessage);
            addGuideStatusRow(diagnostics, tx("guide.extensionClass"), extensionStatus,
                    extensionDetail.isEmpty() ? (currentExtensionClass == null ? tx("guide.diag.noClass") : currentClassName) : extensionDetail);
            addGuideStatusRow(diagnostics, tx("guide.constructor"), constructorStatus, constructorDetail);
            addGuideStatusRow(diagnostics, tx("guide.target"), target[0], target[1]);
            addGuideStatusRow(diagnostics, tx("guide.method"), methodStatus, methodDetail);
            addGuideStatusRow(diagnostics, tx("guide.parameters"), parameters[0], parameters[1]);
            addGuideStatusRow(diagnostics, tx("guide.lastExecution"), executionStatus, executionDetail);

            String problem = "";
            String cause = "";
            String solution = "";
            if ("ERROR".equals(lastExecutionStatus)) {
                problem = tx("guide.diag.lastFailed");
                cause = lastExecutionMessage;
                solution = tx("guide.diag.checkInputs");
            } else if ("ERROR".equals(lastDexVerificationStatus)) {
                problem = tx("guide.diag.dexFailed");
                cause = lastDexVerificationMessage;
                solution = tx("guide.diag.sendDex");
            } else if ("ERROR".equals(lastExtensionLoadStatus)) {
                problem = tx("guide.diag.extensionFailed");
                cause = lastExtensionLoadMessage;
                solution = tx("guide.diag.checkClass");
            } else if ("ERROR".equals(lastDiscoveryStatus)) {
                problem = tx("guide.diag.discoveryFailed");
                cause = lastDiscoveryMessage;
                solution = tx("guide.diag.startPc");
            } else if ("ERROR".equals(connectionStatus)) {
                problem = tx("guide.diag.websocketInactive");
                cause = connectionDetail;
                solution = tx("guide.diag.reconnect");
            } else if (currentExtensionClass != null && targetViewSpinner != null
                    && ("WARNING".equals(target[0]) || "ERROR".equals(target[0]))) {
                problem = tx("guide.diag.noTarget");
                cause = target[1];
                solution = tx("guide.diag.selectTarget");
            } else if ("ERROR".equals(methodStatus) || "WARNING".equals(parameters[0])
                    || "ERROR".equals(parameters[0])) {
                problem = tx("guide.diag.methodNeedsAttention");
                cause = "ERROR".equals(methodStatus) ? methodDetail : parameters[1];
                solution = tx("guide.diag.correctValues");
            }
            addGuideSectionTitle(diagnostics, tx("guide.currentProblemHeading"));
            addGuideProblem(diagnostics, problem, cause, solution, problem.isEmpty());

            addGuideSectionTitle(diagnostics, tx("guide.troubleshooting"));
            StringBuilder tips = new StringBuilder();
            if (!isConnected) tips.append(tx("trouble.pc")).append('\n');
            if (!lastConnectedHost.isEmpty() && !isConnected) {
                tips.append(tx("trouble.websocket")).append('\n');
            }
            if (!"PASS".equals(lastDiscoveryStatus) && !"NOT TESTED".equals(lastDiscoveryStatus)) {
                tips.append(tx("trouble.discovery")).append('\n');
            }
            if (lastBinaryData == null || lastBinaryData.length == 0) {
                tips.append(tx("trouble.dex")).append('\n');
            } else if ("ERROR".equals(lastDexVerificationStatus)) {
                tips.append(tx("trouble.dexVerify")).append('\n');
            } else if (!"PASS".equals(lastDexVerificationStatus)) {
                tips.append(tx("trouble.dexAction")).append('\n');
            }
            if ("ERROR".equals(lastExtensionLoadStatus) || (lastSavedDexFile != null && currentExtensionClass == null)) {
                tips.append(tx("trouble.extension")).append('\n');
            }
            if ("WARNING".equals(target[0]) || "ERROR".equals(target[0])) {
                tips.append(tx("trouble.target")).append('\n');
            }
            if ("ERROR".equals(methodStatus) || "WARNING".equals(parameters[0]) || "ERROR".equals(parameters[0])) {
                tips.append(tx("trouble.method")).append('\n');
            }
            if ("ERROR".equals(lastExecutionStatus)) {
                tips.append(tx("trouble.execution")).append('\n');
            }
            if (tips.length() == 0) tips.append(tx("trouble.none"));
            TextView tipsView = new TextView(activity);
            tipsView.setText(tips.toString().trim());
            tipsView.setTextSize(10.5f);
            tipsView.setTextColor(Color.parseColor("#475569"));
            tipsView.setPadding(dipToPx(8), dipToPx(5), dipToPx(8), dipToPx(5));
            diagnostics.addView(tipsView);

            TextView detailsToggle = new TextView(activity);
            detailsToggle.setText((technicalExpanded[0] ? "▾ " : "▸ ") + tx("guide.technicalDetails")
                    + " (" + tx(technicalExpanded[0] ? "guide.collapseDetails" : "guide.expandDetails") + ")");
            detailsToggle.setTextSize(11f);
            detailsToggle.setTypeface(null, Typeface.BOLD);
            detailsToggle.setTextColor(Color.parseColor("#1D4ED8"));
            detailsToggle.setPadding(dipToPx(8), dipToPx(9), dipToPx(8), dipToPx(9));
            detailsToggle.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    technicalExpanded[0] = !technicalExpanded[0];
                    renderGuideDiagnostics(diagnostics, technicalExpanded);
                }
            });
            diagnostics.addView(detailsToggle);
            if (technicalExpanded[0]) {
                StringBuilder details = new StringBuilder();
                appendGuideDetail(details, "Host", lastConnectedHost);
                appendGuideDetail(details, "Port", lastConnectedPort > 0 ? String.valueOf(lastConnectedPort) : "");
                appendGuideDetail(details, "Connection", connectionStatus + " (isConnected=" + isConnected + ")");
                appendGuideDetail(details, "Socket", socketDetail);
                appendGuideDetail(details, "DEX size", lastBinaryData != null ? lastBinaryData.length + " bytes" : "");
                appendGuideDetail(details, "DEX file", lastSavedDexFile != null ? lastSavedDexFile.getAbsolutePath() : "");
                appendGuideDetail(details, "DEX verification", lastDexVerificationStatus + (lastDexVerificationMessage.isEmpty() ? "" : " — " + lastDexVerificationMessage));
                appendGuideDetail(details, "Loaded class", currentClassName);
                appendGuideDetail(details, "Constructor", lastConstructorSignature);
                View targetView = getSelectedTargetNativeView();
                appendGuideDetail(details, "Target", targetView != null ? targetView.getClass().getName() : "");
                appendGuideDetail(details, "Selected method", selectedMethodName);
                if (selectedMethod != null) {
                    StringBuilder types = new StringBuilder();
                    Class<?>[] p = selectedMethod.getParameterTypes();
                    for (int i = 0; i < p.length; i++) {
                        if (i > 0) types.append(", ");
                        types.append(p[i].getSimpleName());
                    }
                    appendGuideDetail(details, "Parameter types", types.toString());
                }
                appendGuideDetail(details, "Current error",
                        currentErrorMessage.isEmpty() ? "None" : currentErrorMessage);
                appendGuideDetail(details, "Previous/Last error",
                        previousErrorMessage.isEmpty() ? lastErrorMessage : previousErrorMessage);
                appendGuideDetail(details, "Last execution", lastExecutionStatus
                        + (lastExecutionMethod.isEmpty() ? "" : " — " + lastExecutionMethod)
                        + (lastExecutionMessage.isEmpty() ? "" : " — " + lastExecutionMessage));
                appendGuideDetail(details, "Last result", lastExecutionResult);
                TextView detailView = new TextView(activity);
                detailView.setText(details.length() == 0 ? tx("technical.none") : details.toString().trim());
                detailView.setTextSize(10f);
                detailView.setTextColor(Color.parseColor("#475569"));
                detailView.setPadding(dipToPx(8), dipToPx(6), dipToPx(8), dipToPx(6));
                detailView.setBackgroundColor(Color.parseColor("#F1F5F9"));
                diagnostics.addView(detailView);
            }

        } catch (Exception e) {
            diagnostics.removeAllViews();
            TextView unavailable = new TextView(activity);
            unavailable.setText(tx("diagnostic.unavailable"));
            unavailable.setTextColor(Color.parseColor("#B91C1C"));
            unavailable.setTextSize(11f);
            diagnostics.addView(unavailable);
        }
    }

    private void appendGuideDetail(StringBuilder details, String label, String value) {
        if (value != null && !value.trim().isEmpty()) {
            details.append(localizeMessage(label)).append(": ").append(localizeMessage(value)).append('\n');
        }
    }

    private int getGuideUserParameterOffset(Method method) {
        if (method == null) return 0;
        Class<?>[] types = method.getParameterTypes();
        return types.length > 0 && (AndroidViewComponent.class.isAssignableFrom(types[0])
                || View.class.isAssignableFrom(types[0])) ? 1 : 0;
    }

    private List<String> getAvailableGuideGoals(Method method) {
        List<String> goals = new ArrayList<String>();
        goals.add("project");
        if (!currentLoadedMethodEntries.isEmpty()) goals.add("browse");
        if (method != null) {
            int offset = getGuideUserParameterOffset(method);
            if (method.getParameterTypes().length > offset) {
                goals.add("parameters");
                goals.add("values");
                boolean hasProvider = false;
                for (int i = offset; i < method.getParameterTypes().length; i++) {
                    if (!getVerifiedProviderCandidates(method, method.getParameterTypes()[i]).isEmpty()) {
                        hasProvider = true;
                        break;
                    }
                }
                if (hasProvider) goals.add("providers");

                ensureDiscoveredGetterMetadata();
                boolean hasGetter = false;
                for (int i = offset; i < method.getParameterTypes().length && !hasGetter; i++) {
                    for (BlockPropertyMetadata property : blockPropertyMetadata) {
                        if (!"read-write".equals(property.access) && !"read-only".equals(property.access)) continue;
                        Method getter = findPropertyAccessor(property, true);
                        if (getter != null && isProviderReturnTypeCompatible(getter.getReturnType(), method.getParameterTypes()[i])) {
                            hasGetter = true;
                            break;
                        }
                    }
                }
                if (hasGetter) goals.add("getters");
            }
            goals.add("order");
        }
        if ("ERROR".equals(lastExecutionStatus) || (currentErrorMessage != null && !currentErrorMessage.trim().isEmpty())) {
            goals.add("diagnose");
        }
        goals.add("unknown");
        return goals;
    }

    private String getGuideGoalLabel(String goal) {
        if ("project".equals(goal)) return tx("guide.dynamic.goal.project");
        if ("browse".equals(goal)) return tx("guide.dynamic.goal.browse");
        if ("parameters".equals(goal)) return tx("guide.dynamic.goal.parameters");
        if ("values".equals(goal)) return tx("guide.dynamic.goal.values");
        if ("providers".equals(goal)) return tx("guide.dynamic.goal.providers");
        if ("getters".equals(goal)) return tx("guide.dynamic.goal.getters");
        if ("order".equals(goal)) return tx("guide.dynamic.goal.order");
        if ("diagnose".equals(goal)) return tx("guide.dynamic.goal.diagnose");
        return tx("guide.dynamic.unknownGoal");
    }

    private void appendGuideParameterDetails(StringBuilder text, Method method, boolean showValues, int selectedUserArgumentIndex) {
        if (method == null) return;
        Class<?>[] types = method.getParameterTypes();
        List<String> reflectedNames = extractParamNames(method);
        int offset = getGuideUserParameterOffset(method);
        List<String> currentValues = new ArrayList<String>();
        try {
            currentValues = parseArgumentTokens(argsEditText != null ? argsEditText.getText().toString().trim() : "");
        } catch (Exception ignored) {}

        if (types.length == offset) {
            text.append(tx("guide.dynamic.noParameters")).append('\n');
            return;
        }
        int boundedArgumentIndex = Math.max(0, Math.min(selectedUserArgumentIndex, types.length - offset - 1));
        for (int i = offset + boundedArgumentIndex; i < offset + boundedArgumentIndex + 1; i++) {
            int argumentIndex = i - offset;
            boolean metadataMatchesDex = !lastDexChecksum.isEmpty() && lastDexChecksum.equals(lastMetadataDexChecksum)
                    && !lastMetadataEnvelope.isEmpty();
            BlockArgumentMetadata metadata = metadataMatchesDex ? getBlockArgumentMetadata(method, i) : null;
            String name = metadata != null && metadata.name != null && !metadata.name.trim().isEmpty()
                    ? metadata.name : (i < reflectedNames.size() ? reflectedNames.get(i) : tx("guide.dynamic.parameter", argumentIndex + 1));
            text.append("\n• ").append(name).append(" — ").append(types[i].getName()).append('\n');
            if (metadata == null) {
                text.append("  ").append(tx("guide.dynamic.incompleteParameter")).append('\n');
                if (YailList.class.isAssignableFrom(types[i])) {
                    text.append("  ").append(tx("guide.dynamic.listFormatUnknown")).append('\n');
                }
            } else {
                if (metadata.blockType != null && !metadata.blockType.trim().isEmpty()) text.append("  ").append(tx("metadata.blockType")).append(": ").append(metadata.blockType).append('\n');
                if (metadata.elementType != null && !metadata.elementType.trim().isEmpty()) text.append("  ").append(tx("metadata.elementType")).append(": ").append(metadata.elementType).append('\n');
                if (metadata.elementFormat != null && !metadata.elementFormat.trim().isEmpty()) text.append("  ").append(tx("metadata.elementFormat")).append(": ").append(metadata.elementFormat).append('\n');
                boolean hasMetadata = false;
                if (metadata.minimum != null || metadata.maximum != null) {
                    text.append("  ").append(tx("metadata.range")).append(": ").append(metadata.minimum == null ? tx("common.notSpecified") : metadata.minimum)
                            .append(" … ").append(metadata.maximum == null ? tx("common.notSpecified") : metadata.maximum).append('\n');
                    hasMetadata = true;
                }
                if (metadata.defaultValue != null && !metadata.defaultValue.trim().isEmpty()) {
                    try {
                        convertArgument(metadata.defaultValue, types[i]);
                        text.append("  ").append(tx("metadata.verifiedDefault")).append(": ").append(metadata.defaultValue).append('\n');
                        hasMetadata = true;
                    } catch (Exception invalidDefault) {
                        text.append("  ").append(tx("guide.dynamic.defaultInvalid")).append('\n');
                    }
                } else {
                    text.append("  ").append(tx("guide.dynamic.noDefault")).append('\n');
                }
                if (!hasMetadata && (metadata.blockType == null || metadata.blockType.trim().isEmpty())
                        && (metadata.elementType == null || metadata.elementType.trim().isEmpty())
                        && (metadata.elementFormat == null || metadata.elementFormat.trim().isEmpty())) {
                    text.append("  ").append(tx("guide.dynamic.incompleteParameter")).append('\n');
                }
                text.append("  ").append(tx("guide.dynamic.requiredUnknown")).append('\n');
            }
            if (showValues && argumentIndex < currentValues.size()) {
                text.append("  ").append(tx("guide.dynamic.currentValue")).append(' ').append(currentValues.get(argumentIndex)).append('\n');
            }
            List<Method> providers = getVerifiedProviderCandidates(method, types[i]);
                text.append("  ").append(tx("guide.dynamic.providers")).append(": ").append(providers.size()).append('\n');
            for (Method provider : providers) {
                text.append("    · ").append(formatMethodIdentity(provider)).append('\n');
            }
            if (!providers.isEmpty()) {
                text.append("  ").append(tx("guide.dynamic.providerTypeOnly")).append('\n');
            }
            String selectedGetter = selectedGetterProperties.get(getPropertySelectionKey(method, argumentIndex));
            if (selectedGetter != null) text.append("  ").append(tx("guide.dynamic.selectedGetter")).append(' ').append(selectedGetter).append('\n');
        }
    }

    private String getGuideMethodDescription(Method method) {
        if (method == null) return "";
        try {
            SimpleFunction annotation = method.getAnnotation(SimpleFunction.class);
            if (annotation != null && annotation.description() != null && !annotation.description().trim().isEmpty()) {
                return annotation.description();
            }
            com.google.appinventor.components.annotations.SimpleProperty property =
                    method.getAnnotation(com.google.appinventor.components.annotations.SimpleProperty.class);
            return property == null ? "" : property.description();
        } catch (Exception ignored) {
            return "";
        }
    }

    private String normalizeGuideSearchText(String value) {
        if (value == null) return "";
        return value.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}_]+", " ").trim().replaceAll("\\s+", " ");
    }

    private void renderGuideGoalMatches(TextView destination, String userGoal, List<Method> methods) {
        String normalizedGoal = normalizeGuideSearchText(userGoal);
        StringBuilder result = new StringBuilder();
        if (normalizedGoal.length() < 3) {
            result.append(tx("guide.dynamic.searchIntro"));
        } else {
            List<Method> textMatches = new ArrayList<Method>();
            for (Method method : methods) {
                String normalizedName = normalizeGuideSearchText(method.getName());
                String description = normalizeGuideSearchText(getGuideMethodDescription(method));
                boolean nameMatch = !normalizedName.isEmpty()
                        && (normalizedGoal.equals(normalizedName) || (" " + normalizedGoal + " ").contains(" " + normalizedName + " "));
                boolean descriptionMatch = !description.isEmpty() && description.contains(normalizedGoal);
                if (nameMatch || descriptionMatch) textMatches.add(method);
            }
            if (textMatches.isEmpty()) {
                result.append(tx("guide.dynamic.noGoalMatch"));
            } else {
                result.append(tx("guide.dynamic.candidateHeader")).append('\n');
                for (Method method : textMatches) {
                    result.append("• ").append(formatMethodIdentity(method)).append('\n');
                    String description = getGuideMethodDescription(method);
                    if (!description.trim().isEmpty()) result.append("  ").append(tx("guide.dynamic.description")).append(' ').append(description).append('\n');
                }
                result.append(tx("guide.dynamic.selectCandidate")).append('\n');
            }
        }
        destination.setText(result.toString().trim());
    }

    private void renderProjectGuidance(LinearLayout destination, String goal, Method method,
                                       int selectedUserArgumentIndex, String openedDexChecksum, String openedClassName) {
        destination.removeAllViews();
        StringBuilder text = new StringBuilder();
        if (!openedDexChecksum.equals(lastDexChecksum) || !openedClassName.equals(currentClassName)) {
            text.append(tx("guide.dynamic.staleContext")).append('\n');
            addGuideProblem(destination, text.toString(), "", "", false);
            return;
        }

        String goalText = getGuideGoalLabel(goal);
        text.append(tx("guide.dynamic.goalPrefix")).append(' ').append(goalText).append("\n\n");
        if ("project".equals(goal) || "browse".equals(goal) || "unknown".equals(goal)) {
            String projectDescription = "";
            try {
                DesignerComponent component = currentExtensionClass != null
                        ? currentExtensionClass.getAnnotation(DesignerComponent.class) : null;
                if (component != null) projectDescription = component.description();
            } catch (Exception ignored) {}
            if (!projectDescription.trim().isEmpty()) {
                text.append(tx("guide.dynamic.description")).append(' ').append(projectDescription).append("\n\n");
            } else {
                text.append(tx("guide.dynamic.purposeUnknown")).append("\n\n");
            }
            if (currentLoadedMethodEntries.isEmpty()) {
                text.append(tx("guide.dynamic.noMethods")).append('\n');
            } else {
                text.append(tx("guide.dynamic.methodCount", currentLoadedMethodEntries.size())).append(' ')
                        .append(method == null ? tx("guide.dynamic.noneSelected") : formatMethodIdentity(method)).append("\n");
            }
            text.append(tx("guide.dynamic.goalChoose")).append('\n');
        }

        if (method != null) {
            String description = getGuideMethodDescription(method);
            if ("browse".equals(goal) || "parameters".equals(goal) || "values".equals(goal)
                    || "providers".equals(goal) || "getters".equals(goal) || "order".equals(goal)
                    || "diagnose".equals(goal)) {
                text.append('\n').append(tx("guide.dynamic.selectedMethod")).append(' ').append(formatMethodIdentity(method)).append('\n');
                text.append(tx("guide.dynamic.returnType")).append(' ').append(method.getReturnType().getName()).append('\n');
                text.append(description.trim().isEmpty()
                        ? tx("guide.dynamic.methodDescriptionMissing") + "\n"
                        : tx("guide.dynamic.description") + " " + description + "\n");
                if (getGuideUserParameterOffset(method) == 1) {
                    String[] targetState = getGuideTargetState(method);
                    text.append(tx("guide.dynamic.targetParameter")).append(' ').append(method.getParameterTypes()[0].getName())
                            .append("; ").append(tx("guide.dynamic.targetCompatibility")).append(' ').append(targetState[0]).append(" — ")
                            .append(targetState[1]).append('\n');
                }
            }

            if ("parameters".equals(goal) || "values".equals(goal)) {
                text.append('\n').append(tx("guide.dynamic.signatureBasis")).append('\n');
                appendGuideParameterDetails(text, method, "values".equals(goal), selectedUserArgumentIndex);
                if ("values".equals(goal)) text.append('\n').append(tx("guide.dynamic.editValues")).append('\n');
            }

            if ("providers".equals(goal)) {
                int offset = getGuideUserParameterOffset(method);
                boolean found = false;
                int parameterEnd = Math.min(method.getParameterTypes().length, offset + selectedUserArgumentIndex + 1);
                for (int i = offset + selectedUserArgumentIndex; i < parameterEnd; i++) {
                    List<Method> providers = getVerifiedProviderCandidates(method, method.getParameterTypes()[i]);
                    if (providers.isEmpty()) continue;
                    found = true;
                    int userIndex = i - offset;
                    text.append("\n").append(tx("guide.dynamic.providerInput")).append(' ').append(userIndex + 1).append(" (type ")
                            .append(method.getParameterTypes()[i].getName()).append(")\n");
                    for (Method provider : providers) {
                        text.append("• ").append(formatMethodIdentity(provider)).append("\n  ").append(tx("guide.dynamic.providerReturn")).append(' ')
                                .append(provider.getReturnType().getName()).append("; ").append(tx("guide.dynamic.targetType")).append(' ')
                                .append(method.getParameterTypes()[i].getName()).append("\n");
                    }
                    ProviderConfiguration selected = selectedProviderConfigurations.get(userIndex);
                    boolean currentMapping = (lastDexChecksum + "|" + formatMethodIdentity(method)).equals(providerSelectionMainMethod);
                    if (currentMapping && selected != null && providers.contains(selected.providerMethod)) {
                        text.append(tx("guide.dynamic.selectedProvider")).append(' ').append(formatMethodIdentity(selected.providerMethod)).append('\n');
                        for (ProviderArgumentInput input : selected.inputs) {
                            text.append("  · ").append(input.label).append(" — ").append(input.javaType.getName());
                            if (input.metadata == null) text.append(" (").append(tx("guide.dynamic.metadataIncompleteShort")).append(')');
                            else if (input.metadata.defaultValue == null) text.append(" (").append(tx("guide.dynamic.defaultMissingShort")).append(')');
                            if (input.value != null && !input.value.trim().isEmpty()) text.append("; ").append(tx("guide.dynamic.configuredValue")).append(' ').append(input.value);
                            text.append('\n');
                        }
                    }
                }
                if (!found) text.append(tx("guide.dynamic.noProvider")).append('\n');
                text.append(tx("guide.dynamic.providerTypeOnly")).append('\n');
            }

            if ("getters".equals(goal)) {
                ensureDiscoveredGetterMetadata();
                int offset = getGuideUserParameterOffset(method);
                boolean found = false;
                int parameterEnd = Math.min(method.getParameterTypes().length, offset + selectedUserArgumentIndex + 1);
                for (int i = offset + selectedUserArgumentIndex; i < parameterEnd; i++) {
                    for (BlockPropertyMetadata property : blockPropertyMetadata) {
                        if (!"read-write".equals(property.access) && !"read-only".equals(property.access)) continue;
                        Method getter = findPropertyAccessor(property, true);
                        if (getter == null || !isProviderReturnTypeCompatible(getter.getReturnType(), method.getParameterTypes()[i])) continue;
                        found = true;
                        text.append("\n").append(tx("guide.dynamic.getterInput")).append(" → argument #").append(i - offset + 1).append(" (type ")
                                .append(method.getParameterTypes()[i].getName()).append("): ")
                                .append(property.name).append(" — ").append(formatMethodIdentity(getter)).append("; ").append(tx("guide.dynamic.providerReturn"))
                                .append(getter.getReturnType().getName()).append(".\n");
                    }
                }
                if (!found) text.append(tx("guide.dynamic.noGetter")).append('\n');
                text.append(tx("guide.dynamic.getterExisting")).append('\n');
            }

            if ("order".equals(goal)) {
                boolean currentMapping = (lastDexChecksum + "|" + formatMethodIdentity(method)).equals(providerSelectionMainMethod);
                if (currentMapping && !selectedProviderConfigurations.isEmpty()) {
                    text.append('\n').append(tx("guide.dynamic.executionOrder")).append('\n');
                    List<ProviderSelectionSlot> slots = new ArrayList<ProviderSelectionSlot>(providerSelectionSlots);
                    Collections.sort(slots, new java.util.Comparator<ProviderSelectionSlot>() {
                        @Override public int compare(ProviderSelectionSlot a, ProviderSelectionSlot b) {
                            return a.argumentIndex < b.argumentIndex ? -1 : (a.argumentIndex == b.argumentIndex ? 0 : 1);
                        }
                    });
                    for (ProviderSelectionSlot slot : slots) {
                        ProviderConfiguration selected = selectedProviderConfigurations.get(slot.argumentIndex);
                        if (selected != null) text.append(tx("guide.dynamic.providerCall", formatMethodIdentity(selected.providerMethod), slot.argumentIndex + 1)).append('\n');
                    }
                    text.append(tx("guide.dynamic.finalMainMethod")).append(' ').append(formatMethodIdentity(method)).append('\n');
                } else {
                    text.append(tx("guide.dynamic.directExecution")).append(' ')
                            .append(formatMethodIdentity(method)).append(".\n");
                }
                text.append(tx("guide.dynamic.guideNoExecute")).append('\n');
            }
        } else if ("parameters".equals(goal) || "values".equals(goal) || "providers".equals(goal)
                || "getters".equals(goal) || "order".equals(goal)) {
            text.append(tx("guide.dynamic.chooseMethodFirst")).append('\n');
        }

        if ("diagnose".equals(goal)) {
            text.append(tx("guide.dynamic.errorActual")).append(' ').append(lastExecutionMessage == null || lastExecutionMessage.trim().isEmpty()
                    ? currentErrorMessage : lastExecutionMessage).append('\n');
            text.append(tx("guide.dynamic.errorChecksum")).append('\n');
            if (method != null) {
                String[] validation = getGuideParameterState(method);
                text.append(tx("guide.dynamic.validation")).append(' ').append(validation[0]).append(" — ").append(validation[1]).append('\n');
            }
            text.append(tx("guide.dynamic.errorCaution")).append('\n');
        }

        if ("unknown".equals(goal)) {
            text.append(tx("guide.dynamic.unknownGoalHint")).append('\n');
        }

        TextView guidance = new TextView(activity);
        guidance.setText(text.toString().trim());
        guidance.setTextSize(12f);
        guidance.setTextColor(Color.parseColor("#334155"));
        guidance.setLineSpacing(dipToPx(2), 1.15f);
        guidance.setPadding(dipToPx(12), dipToPx(10), dipToPx(12), dipToPx(10));
        GradientDrawable card = new GradientDrawable();
        card.setColor("providers".equals(goal) || "diagnose".equals(goal)
                ? Color.parseColor("#FFF7ED") : Color.parseColor("#F8FAFC"));
        card.setCornerRadius(dipToPx(10));
        card.setStroke(dipToPx(1), Color.parseColor("#E2E8F0"));
        guidance.setBackground(card);
        destination.addView(guidance);
    }

    private void showInteractiveGuideDialog() {
        if (activity == null) return;
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(new Runnable() {
                @Override public void run() { showInteractiveGuideDialog(); }
            });
            return;
        }
        currentLanguageCode = loadSavedLanguageCode();
        currentLanguageIndex = Localization.getLanguageIndex(currentLanguageCode);
        if (languageSpinner != null && languageSpinner.getSelectedItemPosition() != currentLanguageIndex) {
            languageSpinner.setSelection(currentLanguageIndex);
        }
        final String openedDexChecksum = lastDexChecksum == null ? "" : lastDexChecksum;
        final String openedClassName = currentClassName == null ? "" : currentClassName;
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
        TextView title = new TextView(activity);
        title.setText(tx("guide.dynamic.title") + " · " + (currentExtensionClass == null ? tx("common.unknownExtension") : currentExtensionClass.getSimpleName()));
        title.setTextSize(15f);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.parseColor("#0F172A"));
        title.setPadding(dipToPx(18), dipToPx(16), dipToPx(18), dipToPx(8));
        builder.setCustomTitle(title);

        ScrollView scroll = new ScrollView(activity);
        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dipToPx(16), dipToPx(7), dipToPx(16), dipToPx(14));

        TextView projectContext = new TextView(activity);
        StringBuilder projectInfo = new StringBuilder();
        projectInfo.append(tx("guide.dynamic.project")).append('\n');
        projectInfo.append(tx("guide.dynamic.class")).append(": ").append(openedClassName.isEmpty() ? tx("guide.dynamic.notLoaded") : openedClassName).append('\n');
        projectInfo.append(tx("guide.dynamic.checksum")).append(": ").append(openedDexChecksum.isEmpty() ? tx("guide.dynamic.notLoaded") : openedDexChecksum).append('\n');
        boolean metadataMatches = !openedDexChecksum.isEmpty() && openedDexChecksum.equals(lastMetadataDexChecksum)
                && !lastMetadataEnvelope.isEmpty();
        projectInfo.append(tx("guide.dynamic.metadataMatch")).append(": ").append(metadataMatches ? tx("guide.dynamic.yes") : tx("guide.dynamic.noMetadata")).append('\n');
        String metadataVersion = tx("guide.dynamic.notLoaded");
        if (metadataMatches) {
            try {
                int version = new JSONObject(lastMetadataEnvelope).optInt("version", -1);
                if (version >= 0) metadataVersion = String.valueOf(version);
            } catch (Exception ignored) {}
        }
        projectInfo.append(tx("guide.dynamic.metadataVersion")).append(": ").append(metadataVersion).append('\n');
        projectInfo.append(tx("guide.dynamic.metadataCounts")).append(": ").append(metadataMatches ? blockArgumentMetadataByMethod.size() : 0)
                .append(" / ").append(metadataMatches ? blockPropertyMetadata.size() : 0).append('\n');
        projectInfo.append(tx("guide.dynamic.reflectedMethods")).append(": ").append(currentLoadedMethodEntries.size());
        projectContext.setText(projectInfo.toString());
        projectContext.setTextSize(11f);
        projectContext.setTextColor(Color.parseColor("#334155"));
        projectContext.setPadding(dipToPx(10), dipToPx(8), dipToPx(10), dipToPx(8));
        GradientDrawable contextBg = new GradientDrawable();
        contextBg.setColor(Color.parseColor("#EFF6FF"));
        contextBg.setCornerRadius(dipToPx(10));
        contextBg.setStroke(dipToPx(1), Color.parseColor("#DBEAFE"));
        projectContext.setBackground(contextBg);
        content.addView(projectContext);

        final Spinner methodChoice = new Spinner(activity);
        final List<Method> guideMethods = new ArrayList<Method>(currentLoadedMethodEntries);
        List<String> methodOptions = new ArrayList<String>();
        methodOptions.add("— " + tx("guide.dynamic.selectMethod") + " —");
        for (Method method : guideMethods) methodOptions.add(formatMethodIdentity(method));
        ArrayAdapter<String> methodChoiceAdapter = new ArrayAdapter<String>(activity,
                android.R.layout.simple_spinner_item, methodOptions);
        methodChoiceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        methodChoice.setAdapter(methodChoiceAdapter);
        applyCompactSelectorStyle(methodChoice);
        int selectedMethodPosition = 0;
        Method currentlySelected = getGuideSelectedMethod();
        if (currentlySelected != null) {
            int index = guideMethods.indexOf(currentlySelected);
            if (index >= 0) selectedMethodPosition = index + 1;
        }
        methodChoice.setSelection(selectedMethodPosition);
        TextView methodLabel = new TextView(activity);
        methodLabel.setText(tx("guide.dynamic.selectMethod"));
        methodLabel.setTextSize(10.5f);
        methodLabel.setTypeface(null, Typeface.BOLD);
        methodLabel.setTextColor(Color.parseColor("#334155"));
        methodLabel.setPadding(0, dipToPx(10), 0, dipToPx(3));
        content.addView(methodLabel);
        content.addView(methodChoice, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final TextView parameterLabel = new TextView(activity);
        parameterLabel.setText(tx("guide.dynamic.selectParameter"));
        parameterLabel.setTextSize(10.5f);
        parameterLabel.setTypeface(null, Typeface.BOLD);
        parameterLabel.setTextColor(Color.parseColor("#334155"));
        parameterLabel.setPadding(0, dipToPx(8), 0, dipToPx(3));
        content.addView(parameterLabel);
        final Spinner parameterChoice = new Spinner(activity);
        content.addView(parameterChoice, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        applyCompactSelectorStyle(parameterChoice);

        final Spinner goalChoice = new Spinner(activity);
        TextView goalLabel = new TextView(activity);
        goalLabel.setText(tx("guide.dynamic.goalPrompt"));
        goalLabel.setTextSize(10.5f);
        goalLabel.setTypeface(null, Typeface.BOLD);
        goalLabel.setTextColor(Color.parseColor("#334155"));
        goalLabel.setPadding(0, dipToPx(8), 0, dipToPx(3));
        content.addView(goalLabel);
        content.addView(goalChoice, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        applyCompactSelectorStyle(goalChoice);

        LinearLayout goalNavigation = new LinearLayout(activity);
        goalNavigation.setOrientation(LinearLayout.HORIZONTAL);
        goalNavigation.setGravity(Gravity.CENTER_VERTICAL);
        final Button previousGoal = new Button(activity);
        previousGoal.setText(tx("guide.dynamic.previous"));
        applyRoundedButtonStyle(previousGoal, Color.parseColor("#E2E8F0"), Color.parseColor("#334155"), 8);
        final TextView goalStep = new TextView(activity);
        goalStep.setGravity(Gravity.CENTER);
        goalStep.setTextSize(9.5f);
        goalStep.setTextColor(Color.parseColor("#64748B"));
        goalNavigation.addView(previousGoal);
        goalNavigation.addView(goalStep, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        final Button nextGoal = new Button(activity);
        nextGoal.setText(tx("guide.dynamic.next"));
        applyRoundedButtonStyle(nextGoal, Color.parseColor("#DBEAFE"), Color.parseColor("#1D4ED8"), 8);
        goalNavigation.addView(nextGoal);
        LinearLayout.LayoutParams goalNavParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        goalNavParams.topMargin = dipToPx(4);
        content.addView(goalNavigation, goalNavParams);

        EditText userGoal = new EditText(activity);
        userGoal.setSingleLine(false);
        userGoal.setMinLines(1);
        userGoal.setMaxLines(3);
        userGoal.setTextSize(11f);
        userGoal.setHint(tx("guide.dynamic.goalHint"));
        userGoal.setContentDescription(tx("guide.dynamic.goalHint"));
        LinearLayout.LayoutParams goalInputParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        goalInputParams.topMargin = dipToPx(3);
        content.addView(userGoal, goalInputParams);
        final TextView goalMatches = new TextView(activity);
        goalMatches.setTextSize(10f);
        goalMatches.setTextColor(Color.parseColor("#475569"));
        goalMatches.setPadding(dipToPx(8), dipToPx(6), dipToPx(8), dipToPx(6));
        renderGuideGoalMatches(goalMatches, "", guideMethods);
        content.addView(goalMatches);
        userGoal.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable editable) {
                renderGuideGoalMatches(goalMatches, editable == null ? "" : editable.toString(), guideMethods);
            }
        });

        final LinearLayout guidanceContent = new LinearLayout(activity);
        guidanceContent.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams guidanceParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        guidanceParams.topMargin = dipToPx(7);
        content.addView(guidanceContent, guidanceParams);

        final Method[] selectedGuideMethod = new Method[]{selectedMethodPosition > 0 ? guideMethods.get(selectedMethodPosition - 1) : null};
        final int[] selectedUserArgumentIndex = new int[]{0};
        final List<String> selectedGoalKey = new ArrayList<String>();
        final Runnable[] refreshParameterOptions = new Runnable[1];
        refreshParameterOptions[0] = new Runnable() {
            @Override public void run() {
                List<String> options = new ArrayList<String>();
                int offset = getGuideUserParameterOffset(selectedGuideMethod[0]);
                if (selectedGuideMethod[0] != null) {
                    Class<?>[] types = selectedGuideMethod[0].getParameterTypes();
                    for (int i = offset; i < types.length; i++) options.add(formatParameterLabel(selectedGuideMethod[0], i));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
                        android.R.layout.simple_spinner_item, options);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                parameterChoice.setAdapter(adapter);
                parameterLabel.setVisibility(options.isEmpty() ? View.GONE : View.VISIBLE);
                parameterChoice.setVisibility(options.isEmpty() ? View.GONE : View.VISIBLE);
                selectedUserArgumentIndex[0] = 0;
                if (!options.isEmpty()) parameterChoice.setSelection(0);
            }
        };
        parameterChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUserArgumentIndex[0] = Math.max(0, position);
                if (!selectedGoalKey.isEmpty()) {
                    int goalPosition = Math.max(0, goalChoice.getSelectedItemPosition());
                    if (goalPosition < selectedGoalKey.size()) renderProjectGuidance(guidanceContent,
                            selectedGoalKey.get(goalPosition), selectedGuideMethod[0], selectedUserArgumentIndex[0],
                            openedDexChecksum, openedClassName);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        final Runnable[] refreshGoalOptions = new Runnable[1];
        refreshGoalOptions[0] = new Runnable() {
            @Override public void run() {
                List<String> goalKeys = getAvailableGuideGoals(selectedGuideMethod[0]);
                List<String> labels = new ArrayList<String>();
                for (String key : goalKeys) labels.add(getGuideGoalLabel(key));
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
                        android.R.layout.simple_spinner_item, labels);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                goalChoice.setAdapter(adapter);
                applyCompactSelectorStyle(goalChoice);
                selectedGoalKey.clear();
                selectedGoalKey.addAll(goalKeys);
                goalStep.setText("1 / " + goalKeys.size());
                previousGoal.setEnabled(false);
                nextGoal.setEnabled(goalKeys.size() > 1);
                renderProjectGuidance(guidanceContent, goalKeys.isEmpty() ? "project" : goalKeys.get(0),
                        selectedGuideMethod[0], selectedUserArgumentIndex[0], openedDexChecksum, openedClassName);
            }
        };
        goalChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < selectedGoalKey.size()) {
                    goalStep.setText((position + 1) + " / " + selectedGoalKey.size());
                    previousGoal.setEnabled(position > 0);
                    nextGoal.setEnabled(position + 1 < selectedGoalKey.size());
                    renderProjectGuidance(guidanceContent, selectedGoalKey.get(position), selectedGuideMethod[0],
                            selectedUserArgumentIndex[0], openedDexChecksum, openedClassName);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        previousGoal.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                int position = goalChoice.getSelectedItemPosition();
                if (position > 0) goalChoice.setSelection(position - 1);
            }
        });
        nextGoal.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                int position = goalChoice.getSelectedItemPosition();
                if (position >= 0 && position + 1 < selectedGoalKey.size()) goalChoice.setSelection(position + 1);
            }
        });
        methodChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGuideMethod[0] = position > 0 && position <= guideMethods.size() ? guideMethods.get(position - 1) : null;
                renderGuideGoalMatches(goalMatches, userGoal.getText().toString(), guideMethods);
                refreshParameterOptions[0].run();
                refreshGoalOptions[0].run();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        refreshParameterOptions[0].run();
        refreshGoalOptions[0].run();

        final LinearLayout diagnostics = new LinearLayout(activity);
        diagnostics.setOrientation(LinearLayout.VERTICAL);
        diagnostics.setVisibility(View.GONE);
        final boolean[] technicalExpanded = new boolean[]{false};
        TextView diagnosticsToggle = new TextView(activity);
        diagnosticsToggle.setText("▸  " + tx("guide.dynamic.diagnostics"));
        diagnosticsToggle.setTextSize(10.5f);
        diagnosticsToggle.setTypeface(null, Typeface.BOLD);
        diagnosticsToggle.setTextColor(Color.parseColor("#1D4ED8"));
        diagnosticsToggle.setPadding(0, dipToPx(10), 0, dipToPx(5));
        diagnosticsToggle.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                boolean expanded = diagnostics.getVisibility() != View.VISIBLE;
                diagnostics.setVisibility(expanded ? View.VISIBLE : View.GONE);
                ((TextView) v).setText((expanded ? "▾  " : "▸  ") + tx("guide.dynamic.diagnostics"));
                if (expanded) renderGuideDiagnostics(diagnostics, technicalExpanded);
            }
        });
        content.addView(diagnosticsToggle);
        content.addView(diagnostics);

        scroll.addView(content);
        builder.setView(scroll);
        builder.setNeutralButton("↻ " + tx("guide.recheck"), new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                mainHandler.post(new Runnable() {
                    @Override public void run() { showInteractiveGuideDialog(); }
                });
            }
        });
        builder.setPositiveButton(tx("guide.gotIt"), null);
        builder.show();
    }

    private boolean isBoltPanelView(View view) {
        if (view == null || panelContainer == null) return false;
        View current = view;
        while (current != null) {
            if (current == panelContainer) return true;
            ViewParent parent = current.getParent();
            if (!(parent instanceof View)) break;
            current = (View) parent;
        }
        return false;
    }

    @SimpleFunction(description = "Opens BoltWS offline project library and saved test history")
    public void OpenProjectLibrary() {
        try {
            if (projectLibraryRepository == null) projectLibraryRepository = new ProjectLibraryRepository(activity);
            ProjectLibraryUi ui = new ProjectLibraryUi(activity, projectLibraryRepository, new ProjectLibraryUi.Snapshot() {
                @Override public byte[] dex() { return lastBinaryData == null ? null : lastBinaryData.clone(); }
                @Override public String metadataEnvelope() { return createOfflineMetadataSnapshot(); }
                @Override public String extensionName() {
                    return currentExtensionClass == null ? "" : currentExtensionClass.getSimpleName();
                }
                @Override public String packageName() {
                    if (currentClassName == null) return "";
                    int dot = currentClassName.lastIndexOf('.');
                    return dot > 0 ? currentClassName.substring(0, dot) : "";
                }
                @Override public String selectedMethod() {
                    Method method = getSelectedMethodEntry();
                    return method == null ? "" : formatMethodIdentity(method);
                }
                @Override public String arguments() {
                    return argsEditText == null ? "" : argsEditText.getText().toString().trim();
                }
                @Override public String providerInfo() { return getProviderHistorySnapshot(); }
            });
            ui.open();
        } catch (Exception e) {
            logToConsole("[LIBRARY-ERR] Could not open project library: " + e.getMessage());
            android.widget.Toast.makeText(activity, "Project Library unavailable: " + e.getMessage(),
                    android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void recordSavedProjectTest(String method, String arguments, String status, String providerInfo, String result, String error) {
        try {
            if (lastBinaryData == null || lastBinaryData.length == 0) return;
            if (projectLibraryRepository == null) projectLibraryRepository = new ProjectLibraryRepository(activity);
            String checksum = getSha256Checksum(lastBinaryData);
            ProjectLibraryRepository.Project project = projectLibraryRepository.findByDexSha256(checksum);
            if (project == null) return;
            projectLibraryRepository.appendTest(project, method, arguments, status, providerInfo, result, error);
        } catch (Exception e) {
            logToConsole("[LIBRARY-HISTORY-ERR] Could not save execution history: " + e.getMessage());
        }
    }

    private String getProviderHistorySnapshot() {
        JSONArray providers = new JSONArray();
        for (ProviderConfiguration configuration : selectedProviderConfigurations.values()) {
            if (configuration == null || configuration.providerMethod == null) continue;
            JSONObject provider = new JSONObject();
            try {
                provider.put("mainArgumentIndex", configuration.mainArgumentIndex);
                provider.put("method", formatMethodIdentity(configuration.providerMethod));
                JSONArray inputs = new JSONArray();
                for (ProviderArgumentInput input : configuration.inputs) {
                    JSONObject value = new JSONObject();
                    value.put("parameterIndex", input.parameterIndex);
                    value.put("name", input.label);
                    value.put("type", input.javaType.getName());
                    value.put("value", input.value);
                    inputs.put(value);
                }
                provider.put("inputs", inputs);
                providers.put(provider);
            } catch (Exception ignored) {}
        }
        return providers.length() == 0 ? "" : providers.toString();
    }

    private String createOfflineMetadataSnapshot() {
        if (!lastMetadataEnvelope.isEmpty() && lastMetadataDexChecksum.equals(lastDexChecksum)) {
            return lastMetadataEnvelope;
        }
        if (currentExtensionClass == null || currentClassName == null || currentLoadedMethodEntries.isEmpty()) return "";
        try {
            JSONObject envelope = new JSONObject();
            envelope.put("type", "bolt.offline-reflection-snapshot");
            envelope.put("version", 1);
            envelope.put("dexMd5", lastDexChecksum);
            JSONArray components = new JSONArray();
            JSONObject component = new JSONObject();
            component.put("type", currentClassName);
            component.put("metadataSource", "loaded DEX reflection; parameter names included only when verified metadata provided them");
            JSONArray methods = new JSONArray();
            for (Method method : currentLoadedMethodEntries) {
                JSONObject entry = new JSONObject();
                entry.put("name", method.getName());
                entry.put("returnType", method.getReturnType().getName());
                JSONArray params = new JSONArray();
                Class<?>[] types = method.getParameterTypes();
                for (int i = 0; i < types.length; i++) {
                    JSONObject parameter = new JSONObject();
                    parameter.put("index", i);
                    parameter.put("type", types[i].getName());
                    BlockArgumentMetadata metadata = getBlockArgumentMetadata(method, i);
                    if (metadata != null && metadata.name != null && !metadata.name.trim().isEmpty()) {
                        parameter.put("name", metadata.name);
                    }
                    params.put(parameter);
                }
                entry.put("params", params);
                methods.put(entry);
            }
            component.put("methods", methods);
            components.put(component);
            envelope.put("components", components);
            return envelope.toString();
        } catch (Exception e) {
            logToConsole("[LIBRARY-METADATA] Could not create reflection snapshot: " + e.getMessage());
            return "";
        }
    }

    private String getSha256Checksum(byte[] data) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);
        StringBuilder result = new StringBuilder();
        for (byte value : hash) result.append(String.format(Locale.ROOT, "%02x", value & 0xff));
        return result.toString();
    }

    private void logToConsole(final String text) {
        if (consoleTextView == null) return;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                String displayText = localizeMessage(text);
                String line = displayText + "\n";
                SpannableStringBuilder ssb = new SpannableStringBuilder(line);
                String lower = text.toLowerCase(Locale.ROOT);

                if (lower.contains("null") || lower.contains("err") || lower.contains("error") || lower.contains("fail") || lower.contains("invalid")) {
                    ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#EF4444")), 0, line.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (lower.contains("[success]") || lower.contains("ready") || lower.contains("[hot-reload]") || lower.contains("[tuned]")) {
                    ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#22C55E")), 0, line.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else if (lower.contains("[target-view]") || lower.contains("[discover]") || lower.contains("[ws]") || lower.contains("[retry]")) {
                    ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#38BDF8")), 0, line.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    ssb.setSpan(new ForegroundColorSpan(Color.parseColor("#4ADE80")), 0, line.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                consoleTextView.append(ssb);

                if (consoleScrollView != null && isConsoleVisible) {
                    consoleScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            consoleScrollView.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                }
            }
        });
    }

    private void capturePristineBaseline(View view) {
        if (view == null) return;
        View current = view;
        int depth = 0;
        while (current != null && depth < 15) {
            captureSingleBackgroundBaseline(current);
            ViewParent parent = current.getParent();
            if (!(parent instanceof View)) break;
            current = (View) parent;
            depth++;
        }
    }

    private void captureSingleBackgroundBaseline(View view) {
        if (view == null || isBoltPanelView(view)) return;
        if (pristineBackgroundCaptured.containsKey(view)) return;

        pristineBackgroundCaptured.put(view, Boolean.TRUE);
        Drawable background = view.getBackground();
        if (background != null) {
            Drawable.ConstantState cs = background.getConstantState();
            if (cs != null) pristineBackgroundCache.put(view, cs);
        }
    }

    private void restorePristineBackground(View view) {
        if (view == null || isBoltPanelView(view)) return;
        Drawable.ConstantState cs = pristineBackgroundCache.get(view);
        try {
            if (cs != null) {
                view.setBackground(cs.newDrawable(activity.getResources()));
            } else {
                view.setBackground(null);
            }
        } catch (Exception ignored) {
            view.setBackground(null);
        }
    }

    private void makeViewDraggableAndZoomable(final View targetView) {
        if (targetView == null) return;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(activity,
                        new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                            private float scaleFactor = 1.0f;
                            @Override
                            public boolean onScale(ScaleGestureDetector detector) {
                                scaleFactor *= detector.getScaleFactor();
                                scaleFactor = Math.max(0.4f, Math.min(scaleFactor, 4.0f));
                                targetView.setScaleX(scaleFactor);
                                targetView.setScaleY(scaleFactor);
                                return true;
                            }
                        });

                targetView.setOnTouchListener(new View.OnTouchListener() {
                    private float dX, dY;
                    private boolean isDragging = false;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        scaleDetector.onTouchEvent(event);
                        if (event.getPointerCount() > 1) {
                            isDragging = false;
                            return true;
                        }
                        switch (event.getActionMasked()) {
                            case MotionEvent.ACTION_DOWN:
                                dX = v.getX() - event.getRawX();
                                dY = v.getY() - event.getRawY();
                                isDragging = true;
                                if (v.getParent() != null) v.getParent().requestDisallowInterceptTouchEvent(true);
                                break;
                            case MotionEvent.ACTION_MOVE:
                                if (isDragging) {
                                    v.animate().x(event.getRawX() + dX).y(event.getRawY() + dY).setDuration(0).start();
                                }
                                break;
                            case MotionEvent.ACTION_UP:
                            case MotionEvent.ACTION_CANCEL:
                                isDragging = false;
                                break;
                        }
                        return true;
                    }
                });
            }
        });
    }

    private void applyCenterTargetLayout(final View target) {
        if (target == null || isInsideListHierarchy(target)) return;
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    int targetW = dipToPx(300);
                    int targetH = dipToPx(220);

                    target.setMinimumWidth(targetW);
                    target.setMinimumHeight(targetH);

                    ViewGroup.LayoutParams lp = target.getLayoutParams();
                    if (lp == null) {
                        lp = new ViewGroup.LayoutParams(targetW, targetH);
                    }
                    if (lp.width <= 0) lp.width = targetW;
                    if (lp.height <= 0) lp.height = targetH;

                    if (lp instanceof LinearLayout.LayoutParams) {
                        LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) lp;
                        llp.gravity = Gravity.CENTER;
                        llp.setMargins(0, dipToPx(16), 0, dipToPx(16));
                        target.setLayoutParams(llp);
                    } else if (lp instanceof FrameLayout.LayoutParams) {
                        FrameLayout.LayoutParams flp = (FrameLayout.LayoutParams) lp;
                        flp.gravity = Gravity.CENTER;
                        target.setLayoutParams(flp);
                    } else {
                        target.setLayoutParams(lp);
                    }

                    String className = target.getClass().getName().toLowerCase(Locale.ROOT);
                    if (className.contains("card") || target instanceof ViewGroup) {
                        ViewGroup vg = (ViewGroup) target;
                        vg.setClipChildren(false);
                        vg.setClipToPadding(false);
                    }

                    ViewParent parent = target.getParent();
                    while (parent instanceof ViewGroup) {
                        ViewGroup parentVg = (ViewGroup) parent;
                        parentVg.setClipChildren(false);
                        parentVg.setClipToPadding(false);
                        if (parentVg instanceof LinearLayout) {
                            ((LinearLayout) parentVg).setGravity(Gravity.CENTER);
                        }
                        parent = parent.getParent();
                    }

                    target.requestLayout();
                    target.invalidate();
                    makeViewDraggableAndZoomable(target);
                } catch (Exception ignored) {}
            }
        });
    }

    @SimpleFunction(description = "Zero-Block Auto Mode: Renders Header, Status Card, and auto-connects without Kodular blocks.")
    public void StartZeroConfigDevMode(AndroidViewComponent containerLayout, AndroidViewComponent targetComponentToApply) {
        AttachAutoTestPanel(containerLayout, targetComponentToApply);
        GetHotspotClientIP();
    }

    private void updateBuiltInStatusCard(final String methodName, final String hostIp, final String statusMsg, final int statusColor) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (cardMethodNameView != null && methodName != null) {
                    lastCardMethodName = methodName;
                    String compact = methodName;
                    if (compact.startsWith("Run: ")) {
                        compact = compact.substring(5).trim();
                    }
                    int parenIdx = compact.indexOf('(');
                    if (parenIdx > 0) {
                        compact = compact.substring(0, parenIdx).trim();
                    }
                    cardMethodNameView.setText(localizeMessage(compact));
                }
                if (cardIpView != null && hostIp != null) {
                    lastCardHostIp = hostIp;
                    cardIpView.setText("\uD83D\uDFE2 " + formatCardHost(hostIp));
                }
                if (cardStatusView != null && statusMsg != null) {
                    lastCardStatusMessage = statusMsg;
                    cardStatusView.setText(localizeMessage(statusMsg));
                    cardStatusView.setTextColor(statusColor);
                }
            }
        });
    }

    private boolean isPanelAlreadyAttached(ViewGroup host) {
        return host != null && attachedPanelHost == host && panelContainer != null
                && panelContainer.getParent() == host;
    }

    private boolean sameTargetView(AndroidViewComponent first, AndroidViewComponent second) {
        if (first == second) return true;
        return first != null && second != null && first.getView() == second.getView();
    }

    @SimpleFunction(description = "Automatically scans the network to find PC IP")
    public void GetHotspotClientIP() {
        lastDiscoveryStatus = "WARNING";
        lastDiscoveryMessage = "Network scan is running.";
        new Thread(new Runnable() {
            @Override
            public void run() {
                postToMain(new Runnable() {
                    @Override
                    public void run() {
                        DiscoverySent("Scanning network for Bolt PC...", DEFAULT_WS_PORT);
                        logToConsole("[DISCOVER] Scanning network for Bolt PC...");
                        updateBuiltInStatusCard(null, "Searching...", "Searching for PC...", Color.parseColor("#E11D48"));
                    }
                });

                String foundIp = null;
                List<String> subnets = getLocalSubnets();

                for (String subnet : subnets) {
                    foundIp = probeSubnetForBolt(subnet);
                    if (foundIp != null) break;
                }

                final String resultIp = foundIp;
                postToMain(new Runnable() {
                    @Override
                    public void run() {
                        if (resultIp != null) {
                            lastDiscoveryStatus = "PASS";
                            lastDiscoveryMessage = "PC discovered at " + resultIp + ".";
                            ClientIPFound(resultIp);
                            logToConsole("[SUCCESS] Found PC at IP: " + resultIp);
                            updateBuiltInStatusCard(null, resultIp, "Connected", Color.parseColor("#0284C7"));
                            Connect(resultIp, DEFAULT_WS_PORT);
                        } else {
                            lastDiscoveryStatus = "ERROR";
                            lastDiscoveryMessage = "No Bolt PC found on the scanned subnets.";
                            Error("Bolt PC not detected.");
                            logToConsole("[ERROR] Bolt PC not detected on port 9000.");
                            updateBuiltInStatusCard(null, "Not Found", "Click Find PC", Color.parseColor("#DC2626"));
                        }
                    }
                });
            }
        }).start();
    }

    private List<String> getLocalSubnets() {
        List<String> subnets = new ArrayList<String>();
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (intf.isLoopback() || !intf.isUp()) continue;
                for (InetAddress addr : Collections.list(intf.getInetAddresses())) {
                    if (!addr.isLoopbackAddress() && addr.getHostAddress().indexOf(':') < 0) {
                        String ip = addr.getHostAddress();
                        int lastDot = ip.lastIndexOf('.');
                        if (lastDot > 0) {
                            String sub = ip.substring(0, lastDot + 1);
                            if (!subnets.contains(sub)) subnets.add(sub);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        if (!subnets.contains("192.168.43.")) subnets.add("192.168.43.");
        return subnets;
    }

    private String probeSubnetForBolt(String subnetPrefix) {
        ExecutorService pool = Executors.newFixedThreadPool(40);
        final String[] match = new String[1];
        final AtomicBoolean stop = new AtomicBoolean(false);

        for (int i = 1; i <= 254; i++) {
            final String target = subnetPrefix + i;
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    if (stop.get()) return;
                    Socket s = null;
                    try {
                        s = new Socket();
                        s.connect(new InetSocketAddress(target, DEFAULT_WS_PORT), 250);
                        match[0] = target;
                        stop.set(true);
                    } catch (Exception ignored) {
                    } finally {
                        if (s != null) {
                            try { s.close(); } catch (Exception ignored) {}
                        }
                    }
                }
            });
        }
        pool.shutdown();
        try { pool.awaitTermination(2500, TimeUnit.MILLISECONDS); } catch (Exception ignored) {}
        return match[0];
    }

    @SimpleFunction(description = "Connects to Bolt WebSocket server")
    public void Connect(final String host, final int port) {
        if (isConnected) Disconnect();

        this.lastConnectedHost = host;
        this.lastConnectedPort = port;
        this.autoReconnectEnabled = true;

        wsWorkerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(host, port);
                    inputStream = socket.getInputStream();
                    outputStream = socket.getOutputStream();

                    byte[] randomKeyBytes = new byte[16];
                    new SecureRandom().nextBytes(randomKeyBytes);
                    String key = Base64.encodeToString(randomKeyBytes, Base64.NO_WRAP);

                    String request = "GET / HTTP/1.1\r\n" +
                            "Host: " + host + ":" + port + "\r\n" +
                            "Upgrade: websocket\r\n" +
                            "Connection: Upgrade\r\n" +
                            "Sec-WebSocket-Key: " + key + "\r\n" +
                            "Sec-WebSocket-Version: 13\r\n\r\n";

                    outputStream.write(request.getBytes("UTF-8"));
                    outputStream.flush();

                    byte[] buffer = new byte[4096];
                    int read = inputStream.read(buffer);
                    if (read == -1) throw new Exception("Server closed connection immediately");

                    String response = new String(buffer, 0, read);
                    if (response.startsWith("HTTP/1.1 101")) {
                        isConnected = true;
                        postToMain(new Runnable() {
                            @Override
                            public void run() {
                                Connected();
                                logToConsole("[WS] Connected to PC: " + host + ":" + port);
                                updateBuiltInStatusCard(null, host, "Companion Connected", Color.parseColor("#0F172A"));
                            }
                        });
                        readFrames();
                    } else {
                        throw new Exception("Handshake failed:\n" + response);
                    }
                } catch (final Exception e) {
                    closeConnection();
                    postToMain(new Runnable() {
                        @Override
                        public void run() {
                            Error("Connection failed: " + e.getMessage());
                            logToConsole("[WS ERROR] " + e.getMessage());
                            updateBuiltInStatusCard(null, host, "Disconnected", Color.parseColor("#DC2626"));
                        }
                    });
                    scheduleAutoReconnect();
                }
            }
        });
        wsWorkerThread.start();
    }

    @SimpleFunction(description = "Disconnect from Bolt server")
    public void Disconnect() {
        autoReconnectEnabled = false;
        closeConnection();
        postToMain(new Runnable() {
            @Override
            public void run() {
                Disconnected(1000, "Normal closure");
                logToConsole("[WS] Disconnected.");
                updateBuiltInStatusCard(null, null, "Disconnected", Color.parseColor("#DC2626"));
            }
        });
    }

    private void scheduleAutoReconnect() {
        if (!autoReconnectEnabled || lastConnectedHost.isEmpty()) return;
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isConnected && autoReconnectEnabled) {
                    logToConsole("[RETRY] Reconnecting to PC...");
                    Connect(lastConnectedHost, lastConnectedPort);
                }
            }
        }, 3000);
    }

    private void readFrames() {
        final String peer = socket != null && socket.getRemoteSocketAddress() != null
                ? socket.getRemoteSocketAddress().toString() : "unknown";
        try {
            while (isConnected && socket != null && !socket.isClosed()) {
                int b1 = inputStream.read();
                if (b1 == -1) break;

                int opcode = b1 & 0x0F;
                int b2 = inputStream.read();
                if (b2 == -1) break;

                boolean masked = (b2 & 0x80) != 0;
                long length = b2 & 0x7F;

                if (length == 126) {
                    length = ((inputStream.read() & 0xFF) << 8) | (inputStream.read() & 0xFF);
                } else if (length == 127) {
                    length = 0;
                    for (int i = 0; i < 8; i++) length = (length << 8) | (inputStream.read() & 0xFF);
                }

                byte[] mask = new byte[4];
                if (masked) inputStream.read(mask);

                byte[] data = new byte[(int) length];
                int totalRead = 0;
                while (totalRead < length) {
                    int r = inputStream.read(data, totalRead, (int) length - totalRead);
                    if (r == -1) break;
                    totalRead += r;
                }

                if (masked) {
                    for (int i = 0; i < data.length; i++) data[i] = (byte) (data[i] ^ mask[i % 4]);
                }

                if (opcode == 1) {
                    final String msg = new String(data, "UTF-8");
                    final long receivedAt = System.currentTimeMillis();
                    postToMain(new Runnable() {
                        @Override
                        public void run() {
                            if (handleBlockMetadataMessage(msg)) return;
                            MessageReceived(msg);
                            long interval = lastInboundTextTimeMillis == 0L
                                    ? -1L : receivedAt - lastInboundTextTimeMillis;
                            lastInboundTextTimeMillis = receivedAt;
                            logToConsole("[WS-IN] peer=" + peer + ", time=" + receivedAt
                                    + ", intervalMs=" + interval + ", DEX=" + lastDexChecksum
                                    + ", payload=" + msg);
                            logToConsole("[MSG] " + msg);
                            if (isHotReloadCommand(msg)) {
                                String checksum = lastDexChecksum != null ? lastDexChecksum : "";
                                if (checksum.isEmpty()) {
                                    logToConsole("[HOT-RELOAD] Ignored command without a received DEX; peer="
                                            + peer + ", payload=" + msg + ".");
                                    return;
                                }
                                if (checksum.equals(lastHotReloadDexChecksum)) {
                                    logToConsole("[HOT-RELOAD] Ignored duplicate command for already loaded DEX="
                                            + checksum + "; peer=" + peer + ", payload=" + msg + ".");
                                    return;
                                }
                                autoPerformHotReload("text from " + peer);
                            }
                        }
                    });
                } else if (opcode == 2) {
                    final String currentChecksum = getMd5Checksum(data);
                    if (!currentChecksum.equals(lastDexChecksum)) {
                        lastDexChecksum = currentChecksum;
                        lastBinaryData = data;
                        lastMetadataEnvelope = "";
                        lastMetadataDexChecksum = "";
                        lastDexVerificationStatus = "NOT TESTED";
                        lastDexVerificationMessage = "New DEX data received; validation has not run.";
                        final int binaryLength = data.length;
                        postToMain(new Runnable() {
                            @Override
                            public void run() {
                                BinaryReceived(binaryLength);
                                logToConsole("[WS-IN] binary peer=" + peer + ", bytes=" + binaryLength
                                        + ", DEX=" + currentChecksum + ".");
                                logToConsole("[BINARY] DEX received (" + binaryLength + " bytes).");
                                updateBuiltInStatusCard(null, null, "DEX Loaded Live", Color.parseColor("#059669"));
                                blockParameterNamesByMethod.clear();
                                blockArgumentMetadataByMethod.clear();
                                selectedProviderConfigurations.clear();
                                providerArgumentValueCache.clear();
                                providerSelectionMainMethod = "";
                                blockPropertyMetadata.clear();
                                selectedGetterProperties.clear();
                                blockPropertyMetadataDexChecksum = "";
                                autoPerformHotReload("new binary DEX from " + peer);
                                mainHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!currentChecksum.equals(lastMetadataDexChecksum)) {
                                            logToConsole("[METADATA] No bolt.parameter-metadata frame received for DEX="
                                                    + currentChecksum + "; showing generic 'Parameter N (Type)' fallbacks. To get correct parameter names, run the FAST live server with 'node fast-live.js' instead of 'bolt run'.");
                                        }
                                    }
                                }, 2000L);
                            }
                        });
                    }
                } else if (opcode == 8) {
                    break;
                }
            }
        } catch (Exception ignored) {
        } finally {
            closeConnection();
            postToMain(new Runnable() {
                @Override
                public void run() {
                    Disconnected(1006, "Connection lost");
                    logToConsole("[WS] Connection lost. Auto-reconnecting...");
                    updateBuiltInStatusCard(null, null, "Reconnecting...", Color.parseColor("#EA580C"));
                }
            });
            scheduleAutoReconnect();
        }
    }

    private void closeConnection() {
        isConnected = false;
        try {
            if (inputStream != null) inputStream.close();
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (Exception ignored) {}
    }

    private void autoPerformHotReload() {
        autoPerformHotReload("unspecified");
    }

    private boolean isHotReloadCommand(String message) {
        if (message == null) return false;
        String command = message.trim();
        return "RELOAD AnyComponent".equalsIgnoreCase(command)
                || "RELOAD: AnyComponent".equalsIgnoreCase(command);
    }

    private void autoPerformHotReload(String reason) {
        long startedAt = System.currentTimeMillis();
        try {
            SaveLastBinaryAsDex();
            VerifySavedDex();
            LoadDexIntoCompanion();
            lastHotReloadDexChecksum = lastDexChecksum;
            long completedAt = System.currentTimeMillis();
            logToConsole("[HOT-RELOAD] Live updated; reason=" + reason + ", DEX="
                    + lastDexChecksum + ", elapsedMs=" + (completedAt - startedAt) + ".");
        } catch (Exception e) {
            logToConsole("[RELOAD ERROR] reason=" + reason + ", DEX=" + lastDexChecksum
                    + ", elapsedMs=" + (System.currentTimeMillis() - startedAt) + ", "
                    + e.getMessage());
        }
    }

    private String getMd5Checksum(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(data);
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(data.length);
        }
    }

    @SimpleFunction(description = "Saves the last received binary data as classes.dex")
    public void SaveLastBinaryAsDex() {
        if (lastBinaryData == null || lastBinaryData.length == 0) return;
        try {
            File dexDir = new File(getCompatibleCodeCacheDir(), "bolt_live");
            if (!dexDir.exists()) dexDir.mkdirs();

            lastSavedDexFile = new File(dexDir, "classes_" + System.currentTimeMillis() + ".dex");
            FileOutputStream fos = new FileOutputStream(lastSavedDexFile);
            fos.write(lastBinaryData);
            fos.flush();
            fos.close();
            lastSavedDexFile.setReadOnly();
            DexSaved(lastSavedDexFile.getAbsolutePath(), lastSavedDexFile.length());
        } catch (Exception e) {
            Error("Failed to save DEX: " + e.getMessage());
        }
    }

    @SimpleFunction(description = "Verifies if the saved file is a valid Android DEX binary")
    public void VerifySavedDex() {
        if (lastSavedDexFile == null || !lastSavedDexFile.exists()) {
            lastDexVerificationStatus = "ERROR";
            lastDexVerificationMessage = "DEX file does not exist.";
            DexVerificationFailed("DEX file does not exist.");
            return;
        }

        try {
            if (lastSavedDexFile.length() < 8) {
                lastDexVerificationStatus = "ERROR";
                lastDexVerificationMessage = "DEX file is too small.";
                DexVerificationFailed("Invalid DEX: File too small.");
                return;
            }

            byte[] header = new byte[8];
            InputStream fis = lastSavedDexFile.toURI().toURL().openStream();
            fis.read(header);
            fis.close();

            if (header[0] == 0x64 && header[1] == 0x65 && header[2] == 0x78 && header[3] == 0x0A) {
                String version = "" + (char)header[4] + (char)header[5] + (char)header[6];
                lastDexVerificationStatus = "PASS";
                lastDexVerificationMessage = "DEX magic header detected (version " + version + ").";
                DexVerified(version, lastSavedDexFile.length());
            } else {
                lastDexVerificationStatus = "ERROR";
                lastDexVerificationMessage = "DEX magic header is invalid.";
                DexInvalid("Invalid DEX Magic header.");
            }
        } catch (Exception e) {
            lastDexVerificationStatus = "ERROR";
            lastDexVerificationMessage = "Verification error: " + e.getMessage();
            DexVerificationFailed("Verification error: " + e.getMessage());
        }
    }

    @SimpleFunction(description = "Loads DEX and updates panel with universal constructor resolution")
    public void LoadDexIntoCompanion() {
        if (lastSavedDexFile == null || !lastSavedDexFile.exists()) return;
        lastExtensionLoadStatus = "WARNING";
        lastExtensionLoadMessage = "Extension class loading is in progress.";
        lastConstructorSignature = "";
        try {
            File optimizedDir = new File(getCompatibleCodeCacheDir(), "bolt_opt");
            if (!optimizedDir.exists()) optimizedDir.mkdirs();

            currentClassLoader = new DexClassLoader(
                    lastSavedDexFile.getAbsolutePath(),
                    optimizedDir.getAbsolutePath(),
                    null,
                    activity.getClassLoader()
            );

            DexFile dexFile = new DexFile(lastSavedDexFile);
            String candidateClass = null;
            Enumeration<String> entries = dexFile.entries();
            while (entries.hasMoreElements()) {
                String entry = entries.nextElement();
                if (!entry.contains("$") && !entry.endsWith(".R") && !entry.contains(".BuildConfig") && !entry.startsWith("android.") && !entry.startsWith("androidx.")) {
                    candidateClass = entry;
                    break;
                }
            }

            if (candidateClass == null) {
                lastExtensionLoadStatus = "ERROR";
                lastExtensionLoadMessage = "No eligible top-level class was found in the DEX.";
                return;
            }

            currentClassName = candidateClass;
            currentExtensionClass = currentClassLoader.loadClass(candidateClass);
            blockParameterNamesByMethod.clear();
            blockArgumentMetadataByMethod.clear();
            selectedProviderConfigurations.clear();
            providerArgumentValueCache.clear();
            providerSelectionMainMethod = "";
            blockPropertyMetadata.clear();
            selectedGetterProperties.clear();
            blockPropertyMetadataDexChecksum = "";

            try {
                Constructor<?> ctor = currentExtensionClass.getConstructor(ComponentContainer.class);
                currentExtensionInstance = ctor.newInstance(this.formInstance);
                lastConstructorSignature = "ComponentContainer";
            } catch (Throwable e1) {
                try {
                    Constructor<?> ctor = currentExtensionClass.getConstructor(Form.class);
                    currentExtensionInstance = ctor.newInstance(this.formInstance);
                    lastConstructorSignature = "Form";
                } catch (Throwable e2) {
                    try {
                        Constructor<?> ctor = currentExtensionClass.getConstructor();
                        currentExtensionInstance = ctor.newInstance();
                        lastConstructorSignature = "no-argument";
                    } catch (Throwable e3) {
                        try {
                            Constructor<?> ctor = currentExtensionClass.getConstructor(Context.class);
                            currentExtensionInstance = ctor.newInstance(this.activity);
                            lastConstructorSignature = "Context";
                        } catch (Throwable e4) {
                            Constructor<?> ctor = currentExtensionClass.getConstructor(Activity.class);
                            currentExtensionInstance = ctor.newInstance(this.activity);
                            lastConstructorSignature = "Activity";
                        }
                    }
                }
            }

            argumentsCache.clear();
            customObjectResults.clear();

            lastExtensionLoadStatus = currentExtensionInstance != null ? "PASS" : "ERROR";
            lastExtensionLoadMessage = currentExtensionInstance != null
                    ? "Extension class and instance loaded."
                    : "Extension instance was not created.";
            ExtensionReady(currentClassName);
            populateAutoPanel();

            updateBuiltInStatusCard("Run: " + currentExtensionClass.getSimpleName(), null, "Ready: " + currentExtensionClass.getSimpleName(), Color.parseColor("#059669"));

        } catch (Exception e) {
            lastExtensionLoadStatus = "ERROR";
            lastExtensionLoadMessage = "LoadDex failed: " + e.getMessage();
            Error("LoadDex failed: " + e.getMessage());
        }
    }

    @SimpleFunction(description = "Attaches sleek modern testing panel matching IMG_20261007_200839 UI")
    public void AttachAutoTestPanel(AndroidViewComponent containerLayout, AndroidViewComponent targetComponentToApply) {
        View parentView = containerLayout.getView();

        if (!(parentView instanceof ViewGroup)) {
            Error("Container must be a layout!");
            return;
        }

        ViewGroup layoutGroup = (ViewGroup) parentView;
        boolean panelAlreadyAttached = isPanelAlreadyAttached(layoutGroup);
        boolean targetChanged = !sameTargetView(defaultTargetComponent, targetComponentToApply);
        this.defaultTargetComponent = targetComponentToApply;

        if ((!panelAlreadyAttached || targetChanged)
                && defaultTargetComponent != null && defaultTargetComponent.getView() != null) {
            capturePristineBaseline(defaultTargetComponent.getView());
            applyCenterTargetLayout(defaultTargetComponent.getView());
        }

        if (panelAlreadyAttached) {
            if (targetChanged && targetViewSpinner != null) {
                setupTargetViewsList();
                Method selectedMethod = getSelectedMethodEntry();
                if (selectedMethod != null) autoSuggestBestTargetView(selectedMethod);
            }
            return;
        }

        layoutGroup.removeAllViews();

        panelContainer = new LinearLayout(activity);
        panelContainer.setOrientation(LinearLayout.VERTICAL);
        panelContainer.setPadding(dipToPx(8), dipToPx(4), dipToPx(8), dipToPx(4));
        panelContainer.setBackgroundColor(Color.TRANSPARENT);

        // १. मॉडर्न ग्रेडियंट हेडर कार्ड
        autoHeaderCard = new LinearLayout(activity);
        autoHeaderCard.setOrientation(LinearLayout.VERTICAL);
        autoHeaderCard.setPadding(dipToPx(10), dipToPx(5), dipToPx(10), dipToPx(5));

        GradientDrawable headerBg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor("#075985"), Color.parseColor("#0284C7")}
        );
        headerBg.setCornerRadius(dipToPx(14));
        autoHeaderCard.setBackground(headerBg);
        applyUiElevation(autoHeaderCard, 3);

        cardMethodNameView = new TextView(activity);
        cardMethodNameView.setText(tx("app.ready"));
        cardMethodNameView.setTextSize(9.5f);
        cardMethodNameView.setTypeface(null, Typeface.BOLD);
        cardMethodNameView.setTextColor(Color.WHITE);
        cardMethodNameView.setGravity(Gravity.CENTER);
        autoHeaderCard.addView(cardMethodNameView);

        cardTitleView = new TextView(activity);
        cardTitleView.setText(tx("app.title"));
        cardTitleView.setTextSize(12f);
        cardTitleView.setTypeface(null, Typeface.BOLD);
        cardTitleView.setTextColor(Color.WHITE);
        cardTitleView.setGravity(Gravity.CENTER);
        autoHeaderCard.addView(cardTitleView);

        LinearLayout pillsRow = new LinearLayout(activity);
        pillsRow.setOrientation(LinearLayout.HORIZONTAL);
        pillsRow.setGravity(Gravity.CENTER_VERTICAL);
        pillsRow.setPadding(0, dipToPx(3), 0, 0);

        LinearLayout leftPillsCol = new LinearLayout(activity);
        leftPillsCol.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lpCol = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        leftPillsCol.setLayoutParams(lpCol);

        cardIpView = new TextView(activity);
        cardIpView.setText("🟢 " + tx("pc.notConnected"));
        cardIpView.setTextSize(8.5f);
        cardIpView.setTypeface(null, Typeface.BOLD);
        cardIpView.setTextColor(Color.parseColor("#0369A1"));
        GradientDrawable ipPillBg = new GradientDrawable();
        ipPillBg.setColor(Color.WHITE);
        ipPillBg.setCornerRadius(dipToPx(12));
        cardIpView.setBackground(ipPillBg);
        cardIpView.setPadding(dipToPx(8), dipToPx(2), dipToPx(8), dipToPx(2));
        LinearLayout.LayoutParams ipLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ipLp.rightMargin = dipToPx(5);
        cardIpView.setLayoutParams(ipLp);
        leftPillsCol.addView(cardIpView);

        cardStatusView = new TextView(activity);
        cardStatusView.setText(tx("extension.ready"));
        cardStatusView.setTextSize(8.5f);
        cardStatusView.setSingleLine(true);
        cardStatusView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        cardStatusView.setTextColor(Color.parseColor("#065F46"));
        GradientDrawable stPillBg = new GradientDrawable();
        stPillBg.setColor(Color.parseColor("#D1FAE5"));
        stPillBg.setCornerRadius(dipToPx(12));
        cardStatusView.setBackground(stPillBg);
        cardStatusView.setPadding(dipToPx(8), dipToPx(2), dipToPx(8), dipToPx(2));
        leftPillsCol.addView(cardStatusView);

        pillsRow.addView(leftPillsCol);

        cardFindPcButton = new Button(activity);
        cardFindPcButton.setText("📶  " + tx("button.findPc"));
        cardFindPcButton.setTextSize(10f);
        cardFindPcButton.setTypeface(null, Typeface.BOLD);
        applyRoundedButtonStyle(cardFindPcButton, Color.parseColor("#15803D"), Color.WHITE, 10);
        cardFindPcButton.setTextSize(9f);
        cardFindPcButton.setPadding(dipToPx(8), 0, dipToPx(8), 0);
        cardFindPcButton.setMinHeight(dipToPx(34));
        cardFindPcButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { GetHotspotClientIP(); }
        });
        pillsRow.addView(cardFindPcButton);

        autoHeaderCard.addView(pillsRow);
        panelContainer.addView(autoHeaderCard);

        // २. मुख्य मॉडर्न पॅनेल बॉक्स
        LinearLayout mainCard = new LinearLayout(activity);
        mainCard.setOrientation(LinearLayout.VERTICAL);
        mainCard.setPadding(dipToPx(8), dipToPx(6), dipToPx(8), dipToPx(6));
        GradientDrawable mainCardBg = new GradientDrawable();
        mainCardBg.setColor(Color.WHITE);
        mainCardBg.setCornerRadius(dipToPx(14));
        mainCardBg.setStroke(dipToPx(1), Color.parseColor("#D7E2EF"));
        mainCard.setBackground(mainCardBg);
        applyUiElevation(mainCard, 2);
        LinearLayout.LayoutParams mcLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mcLp.topMargin = dipToPx(5);
        mainCard.setLayoutParams(mcLp);

        // Row 1: टूलबार
        LinearLayout topBar = new LinearLayout(activity);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView termIcon = new TextView(activity);
        termIcon.setText(">_");
        termIcon.setTextSize(11f);
        termIcon.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        termIcon.setTextColor(Color.WHITE);
        termIcon.setGravity(Gravity.CENTER);
        GradientDrawable termIconBg = new GradientDrawable();
        termIconBg.setColor(Color.parseColor("#2563EB"));
        termIconBg.setCornerRadius(dipToPx(8));
        termIcon.setBackground(termIconBg);
        LinearLayout.LayoutParams tiLp = new LinearLayout.LayoutParams(dipToPx(28), dipToPx(28));
        tiLp.rightMargin = dipToPx(6);
        termIcon.setLayoutParams(tiLp);
        topBar.addView(termIcon);

        panelTitleView = new TextView(activity);
        panelTitleView.setText(tx("panel.title"));
        panelTitleView.setTextSize(9.5f);
        panelTitleView.setTypeface(null, Typeface.BOLD);
        panelTitleView.setTextColor(Color.parseColor("#0F172A"));
        topBar.addView(panelTitleView);

        View vDiv = new View(activity);
        vDiv.setBackgroundColor(Color.parseColor("#CBD5E1"));
        LinearLayout.LayoutParams vdParams = new LinearLayout.LayoutParams(dipToPx(1), dipToPx(20));
        vdParams.setMargins(dipToPx(6), 0, dipToPx(6), 0);
        topBar.addView(vDiv, vdParams);

        languageSpinner = new Spinner(activity);
        languageSpinner.setContentDescription(tx("language.title"));
        String[] languages = Localization.getLanguageNames();
        ArrayAdapter<String> langAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_item, languages);
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(langAdapter);
        languageSpinner.setSelection(currentLanguageIndex);
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                if (currentLanguageIndex != pos) {
                    currentLanguageIndex = pos;
                    currentLanguageCode = Localization.getLanguageCode(pos);
                    applyLanguageChange();
                }
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
        topBar.addView(languageSpinner, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        // Info Button (ℹ)
        Button infoBtn = new Button(activity);
        infoBtn.setText("ℹ");
        infoBtn.setTextSize(11f);
        infoBtn.setTypeface(null, Typeface.BOLD);
        applyRoundedButtonStyle(infoBtn, Color.parseColor("#FEF3C7"), Color.parseColor("#D97706"), 12);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(dipToPx(30), dipToPx(28));
        infoLp.setMargins(dipToPx(3), 0, dipToPx(3), 0);
        infoBtn.setLayoutParams(infoLp);
        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                showInteractiveGuideDialog();
            }
        });
        topBar.addView(infoBtn);

        Button libraryBtn = new Button(activity);
        libraryBtn.setText("☰");
        libraryBtn.setContentDescription("Project Library");
        libraryBtn.setTextSize(11f);
        libraryBtn.setTypeface(null, Typeface.BOLD);
        applyRoundedButtonStyle(libraryBtn, Color.parseColor("#DCFCE7"), Color.parseColor("#047857"), 10);
        LinearLayout.LayoutParams libraryLp = new LinearLayout.LayoutParams(dipToPx(34), dipToPx(28));
        libraryLp.setMargins(dipToPx(2), 0, dipToPx(2), 0);
        libraryBtn.setLayoutParams(libraryLp);
        libraryBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { OpenProjectLibrary(); }
        });
        topBar.addView(libraryBtn);

        // Minimize / Expand Button
        minimizeButton = new Button(activity);
        minimizeButton.setText(getLocalizedText("minimize"));
        minimizeButton.setTextSize(9.5f);
        applyRoundedButtonStyle(minimizeButton, Color.parseColor("#DBEAFE"), Color.parseColor("#1D4ED8"), 12);
        minimizeButton.setPadding(dipToPx(8), dipToPx(2), dipToPx(8), dipToPx(2));
        minimizeButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                isPanelMinimized = !isPanelMinimized;
                if (autoHeaderCard != null) {
                    autoHeaderCard.setVisibility(isPanelMinimized ? View.GONE : View.VISIBLE);
                }
                minimizeButton.setText(isPanelMinimized ? getLocalizedText("expand") : getLocalizedText("minimize"));
            }
        });
        topBar.addView(minimizeButton);
        mainCard.addView(topBar);

        // ३. पॅनेल बॉडी
        panelBody = new LinearLayout(activity);
        panelBody.setOrientation(LinearLayout.VERTICAL);

        // Target Layout Card
        LinearLayout targetCard = new LinearLayout(activity);
        targetCard.setOrientation(LinearLayout.HORIZONTAL);
        targetCard.setGravity(Gravity.CENTER_VERTICAL);
        targetCard.setPadding(dipToPx(8), dipToPx(3), dipToPx(8), dipToPx(3));
        GradientDrawable tcBg = new GradientDrawable();
        tcBg.setColor(Color.parseColor("#EFF6FF"));
        tcBg.setCornerRadius(dipToPx(10));
        tcBg.setStroke(dipToPx(1), Color.parseColor("#DBEAFE"));
        targetCard.setBackground(tcBg);
        applyUiElevation(targetCard, 1);
        LinearLayout.LayoutParams tclp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tclp.topMargin = dipToPx(4);
        targetCard.setLayoutParams(tclp);

        TextView gridIcon = new TextView(activity);
        gridIcon.setText("⊞");
        gridIcon.setTextSize(14f);
        gridIcon.setTypeface(null, Typeface.BOLD);
        gridIcon.setTextColor(Color.WHITE);
        gridIcon.setGravity(Gravity.CENTER);
        GradientDrawable giBg = new GradientDrawable();
        giBg.setColor(Color.parseColor("#8B5CF6"));
        giBg.setCornerRadius(dipToPx(6));
        gridIcon.setBackground(giBg);
        LinearLayout.LayoutParams giLp = new LinearLayout.LayoutParams(dipToPx(26), dipToPx(26));
        giLp.rightMargin = dipToPx(6);
        gridIcon.setLayoutParams(giLp);
        targetCard.addView(gridIcon);

        targetViewSpinner = new Spinner(activity);
        applyCompactSelectorStyle(targetViewSpinner);
        rescanTargetsButton = new Button(activity);
        rescanTargetsButton.setText("\u21BB");
        rescanTargetsButton.setContentDescription(tx("button.rescan"));
        rescanTargetsButton.setTextSize(12f);
        applyRoundedButtonStyle(rescanTargetsButton, Color.parseColor("#DBEAFE"), Color.parseColor("#1D4ED8"), 8);
        LinearLayout.LayoutParams rescanLp = new LinearLayout.LayoutParams(dipToPx(34), dipToPx(34));
        rescanLp.rightMargin = dipToPx(5);
        targetCard.addView(rescanTargetsButton, rescanLp);
        targetCard.addView(targetViewSpinner, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        rescanTargetsButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                setupTargetViewsList(true);
                userManuallySelectedTarget = true;
                logToConsole("[TARGET-VIEW] Rescanned currently attached views. Select a target to continue.");
            }
        });
        panelBody.addView(targetCard);

        setupTargetViewsList();

        targetViewSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                userManuallySelectedTarget = true;
                View target = getSelectedTargetNativeView();
                if (target != null) {
                    applyCenterTargetLayout(target);
                    logToConsole("[TARGET-VIEW] Active: " + availableTargetNames.get(position));
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // नवीन: Dropdown उघडल्यावर आपोआप views rescan करा
        // जेणेकरून Kodular मध्ये नवीन Label/Layout add केल्यावर ते लगेच दिसतील.
        targetViewSpinner.setOnTouchListener(new View.OnTouchListener() {
            private boolean isRefreshing = false;
            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                if (event.getActionMasked() == android.view.MotionEvent.ACTION_DOWN) {
                    if (!isRefreshing) {
                        isRefreshing = true;
                        // आधीची निवड लक्षात ठेवा
                        int previousPos = targetViewSpinner.getSelectedItemPosition();
                        String previousName = (previousPos >= 0 && previousPos < availableTargetNames.size())
                                ? availableTargetNames.get(previousPos) : null;
                        // Views पुन्हा scan करा
                        setupTargetViewsList();
                        // जुनं target अजूनही असेल तर तेच निवडा
                        if (previousName != null) {
                            int newPos = availableTargetNames.indexOf(previousName);
                            if (newPos >= 0 && newPos != targetViewSpinner.getSelectedItemPosition()) {
                                targetViewSpinner.setSelection(newPos);
                            }
                        }
                        logToConsole("[TARGET-VIEW] Auto-refreshed view list (" + availableTargetViews.size() + " views).");
                        isRefreshing = false;
                    }
                }
                return false; // Dropdown उघडण्यासाठी event pass करा
            }
        });

        // Method Selector Card
        LinearLayout methodCard = new LinearLayout(activity);
        methodCard.setOrientation(LinearLayout.HORIZONTAL);
        methodCard.setGravity(Gravity.CENTER_VERTICAL);
        methodCard.setPadding(dipToPx(8), dipToPx(3), dipToPx(8), dipToPx(3));
        GradientDrawable mcCardBg = new GradientDrawable();
        mcCardBg.setColor(Color.parseColor("#F0FDF4"));
        mcCardBg.setCornerRadius(dipToPx(10));
        mcCardBg.setStroke(dipToPx(1), Color.parseColor("#DCFCE7"));
        methodCard.setBackground(mcCardBg);
        applyUiElevation(methodCard, 1);
        LinearLayout.LayoutParams mclp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mclp.topMargin = dipToPx(4);
        methodCard.setLayoutParams(mclp);

        TextView codeIcon = new TextView(activity);
        codeIcon.setText("</>");
        codeIcon.setTextSize(10f);
        codeIcon.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        codeIcon.setTextColor(Color.WHITE);
        codeIcon.setGravity(Gravity.CENTER);
        GradientDrawable ciBg = new GradientDrawable();
        ciBg.setColor(Color.parseColor("#10B981"));
        ciBg.setCornerRadius(dipToPx(6));
        codeIcon.setBackground(ciBg);
        LinearLayout.LayoutParams ciLp = new LinearLayout.LayoutParams(dipToPx(26), dipToPx(26));
        ciLp.rightMargin = dipToPx(6);
        codeIcon.setLayoutParams(ciLp);
        methodCard.addView(codeIcon);

        methodSpinner = new Spinner(activity);
        applyCompactSelectorStyle(methodSpinner);
        methodCard.addView(methodSpinner, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Button methodDetailsButton = new Button(activity);
        methodDetailsButton.setText("ℹ");
        methodDetailsButton.setContentDescription("Selected method signature details");
        methodDetailsButton.setTextSize(10f);
        applyRoundedButtonStyle(methodDetailsButton, Color.parseColor("#D1FAE5"), Color.parseColor("#047857"), 8);
        LinearLayout.LayoutParams methodDetailsLp = new LinearLayout.LayoutParams(dipToPx(30), dipToPx(30));
        methodDetailsLp.leftMargin = dipToPx(4);
        methodDetailsButton.setLayoutParams(methodDetailsLp);
        methodDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                showSelectedMethodDetails(getSelectedMethodEntry());
            }
        });
        methodCard.addView(methodDetailsButton);
        panelBody.addView(methodCard);

        final LinearLayout argumentsSection = new LinearLayout(activity);
        argumentsSection.setOrientation(LinearLayout.VERTICAL);
        argumentsSection.setVisibility(View.GONE);
        final TextView argumentsToggle = new TextView(activity);
        argumentsToggle.setText("▸  " + tx("guide.parameters"));
        argumentsToggle.setTextSize(10f);
        argumentsToggle.setTypeface(null, Typeface.BOLD);
        argumentsToggle.setTextColor(Color.parseColor("#475569"));
        argumentsToggle.setGravity(Gravity.CENTER_VERTICAL);
        argumentsToggle.setPadding(dipToPx(2), dipToPx(7), dipToPx(2), dipToPx(5));
        GradientDrawable argumentsToggleBg = new GradientDrawable();
        argumentsToggleBg.setColor(Color.parseColor("#F1F5F9"));
        argumentsToggleBg.setCornerRadius(dipToPx(8));
        argumentsToggle.setBackground(argumentsToggleBg);
        argumentsToggle.setPadding(dipToPx(9), dipToPx(7), dipToPx(9), dipToPx(7));
        argumentsToggle.setContentDescription("Expand or collapse method arguments");
        argumentsToggle.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                boolean expanded = argumentsSection.getVisibility() != View.VISIBLE;
                argumentsSection.setVisibility(expanded ? View.VISIBLE : View.GONE);
                argumentsToggle.setText((expanded ? "▾  " : "▸  ") + tx("guide.parameters"));
            }
        });
        panelBody.addView(argumentsToggle);

        paramInfoLabel = new TextView(activity);
        paramInfoLabel.setText(getLocalizedText("param_guide"));
        paramInfoLabel.setTextSize(9f);
        paramInfoLabel.setTextColor(Color.parseColor("#475569"));
        paramInfoLabel.setMaxLines(2);
        paramInfoLabel.setEllipsize(android.text.TextUtils.TruncateAt.END);
        paramInfoLabel.setPadding(dipToPx(2), dipToPx(3), dipToPx(2), dipToPx(2));
        argumentsSection.addView(paramInfoLabel);

        // Parameters Input Box सह Copy Icon (❐)
        LinearLayout inputRow = new LinearLayout(activity);
        inputRow.setOrientation(LinearLayout.HORIZONTAL);
        inputRow.setGravity(Gravity.CENTER_VERTICAL);
        GradientDrawable inBg = new GradientDrawable();
        inBg.setColor(Color.WHITE);
        inBg.setCornerRadius(dipToPx(10));
        inBg.setStroke(dipToPx(1), Color.parseColor("#CBD5E1"));
        inputRow.setBackground(inBg);
        inputRow.setPadding(dipToPx(8), dipToPx(2), dipToPx(6), dipToPx(2));

        argsEditText = new EditText(activity);
        argsEditText.setTextSize(11f);
        argsEditText.setTextColor(Color.parseColor("#0F172A"));
        argsEditText.setHintTextColor(Color.parseColor("#94A3B8"));
        argsEditText.setBackgroundColor(Color.TRANSPARENT);
        inputRow.addView(argsEditText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        copyArgsBtn = new Button(activity);
        copyArgsBtn.setText("❐");
        copyArgsBtn.setTextSize(12f);
        applyRoundedButtonStyle(copyArgsBtn, Color.parseColor("#F1F5F9"), Color.parseColor("#64748B"), 8);
        copyArgsBtn.setPadding(dipToPx(6), dipToPx(2), dipToPx(6), dipToPx(2));
        LinearLayout.LayoutParams cpLp = new LinearLayout.LayoutParams(dipToPx(28), dipToPx(28));
        copyArgsBtn.setLayoutParams(cpLp);
        copyArgsBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (argsEditText != null && activity != null) {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("BoltArgs", argsEditText.getText().toString());
                    if (clipboard != null) clipboard.setPrimaryClip(clip);
                    logToConsole("[CLIPBOARD] Parameters Copied!");
                }
            }
        });
        inputRow.addView(copyArgsBtn);
        argumentsSection.addView(inputRow);
        panelBody.addView(argumentsSection);

        // Quick Action Tile (🎨 रंग व आकार >)
        openTuningTile = new LinearLayout(activity);
        openTuningTile.setOrientation(LinearLayout.HORIZONTAL);
        openTuningTile.setGravity(Gravity.CENTER_VERTICAL);
        openTuningTile.setPadding(dipToPx(10), dipToPx(5), dipToPx(10), dipToPx(5));
        GradientDrawable tileBg = new GradientDrawable();
        tileBg.setColor(Color.parseColor("#F3E8FF"));
        tileBg.setStroke(dipToPx(1), Color.parseColor("#E9D5FF"));
        tileBg.setCornerRadius(dipToPx(10));
        openTuningTile.setBackground(tileBg);
        applyUiElevation(openTuningTile, 1);
        LinearLayout.LayoutParams optLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        optLp.topMargin = dipToPx(3);
        openTuningTile.setLayoutParams(optLp);

        TextView tuneLabel = new TextView(activity);
        tuneLabel.setText(getLocalizedText("tune"));
        tuneLabel.setTextSize(10f);
        tuneLabel.setTypeface(null, Typeface.BOLD);
        tuneLabel.setTextColor(Color.parseColor("#9333EA"));
        openTuningTile.addView(tuneLabel, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

        openTuningTile.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { showFlexibleTuningDialog(); }
        });
        panelBody.addView(openTuningTile);

        // Action Buttons
        LinearLayout btnRow = new LinearLayout(activity);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setPadding(0, dipToPx(4), 0, dipToPx(1));

        runButton = new Button(activity);
        runButton.setText(getLocalizedText("execute"));
        runButton.setTextSize(11f);
        runButton.setTypeface(null, Typeface.BOLD);
        GradientDrawable runBg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#2563EB"), Color.parseColor("#06B6D4")}
        );
        runBg.setCornerRadius(dipToPx(12));
        runButton.setBackground(runBg);
        runButton.setTextColor(Color.WHITE);
        applyUiElevation(runButton, 3);
        LinearLayout.LayoutParams p1 = new LinearLayout.LayoutParams(0, dipToPx(36), 1.1f);
        p1.rightMargin = dipToPx(4);
        runButton.setLayoutParams(p1);
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { executeSelectedMethodWithProvidersFromUI(); }
        });
        btnRow.addView(runButton);

        resetButton = new Button(activity);
        resetButton.setText(getLocalizedText("reset"));
        resetButton.setTextSize(10.5f);
        resetButton.setTypeface(null, Typeface.BOLD);
        applyRoundedButtonStyle(resetButton, Color.parseColor("#E2E8F0"), Color.parseColor("#1E293B"), 12);
        LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, dipToPx(36), 1.0f);
        resetButton.setLayoutParams(p2);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { ResetTargetView(); }
        });
        btnRow.addView(resetButton);
        panelBody.addView(btnRow);

        // Console
        liveConsoleLayout = new LinearLayout(activity);
        liveConsoleLayout.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable conBg = new GradientDrawable();
        conBg.setColor(Color.parseColor("#0F172A"));
        conBg.setCornerRadius(dipToPx(10));
        liveConsoleLayout.setBackground(conBg);
        liveConsoleLayout.setPadding(dipToPx(7), dipToPx(4), dipToPx(7), dipToPx(4));
        LinearLayout.LayoutParams lclLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lclLp.topMargin = dipToPx(4);
        liveConsoleLayout.setLayoutParams(lclLp);

        consoleScrollView = new ScrollView(activity);
        consoleScrollView.setFillViewport(true);
        consoleTextView = new TextView(activity);
        consoleTextView.setText(localizeMessage("[WS] Ready: Bolt Live Engine v155 (Universal Chameleon Active)") + "\n");
        consoleTextView.setTextSize(8.5f);
        consoleTextView.setTextColor(Color.parseColor("#38BDF8"));
        consoleTextView.setTypeface(Typeface.MONOSPACE);
        consoleScrollView.addView(consoleTextView);
        // BOLTWSCONSOLE_LONG_PRESS_COPY
        consoleTextView.setTextIsSelectable(false);
        consoleTextView.setOnLongClickListener(
                new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                try {
                    CharSequence value = consoleTextView.getText();
                    String fullLog = value == null ? "" : value.toString();

                    if (fullLog.trim().isEmpty()) {
                        android.widget.Toast.makeText(
                                activity, "Console is empty",
                                android.widget.Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    android.content.ClipboardManager clipboard =
                            (android.content.ClipboardManager)
                                    activity.getSystemService(
                                            Context.CLIPBOARD_SERVICE);

                    if (clipboard == null) {
                        android.widget.Toast.makeText(
                                activity, "Clipboard unavailable",
                                android.widget.Toast.LENGTH_SHORT).show();
                        return true;
                    }

                    clipboard.setPrimaryClip(
                            android.content.ClipData.newPlainText(
                                    "BoltWS Console Log", fullLog));

                    android.widget.Toast.makeText(
                            activity, "Complete Console Log Copied",
                            android.widget.Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    android.widget.Toast.makeText(
                            activity, "Copy failed: " + e.getMessage(),
                            android.widget.Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
        LinearLayout consoleHeading = new LinearLayout(activity);
        consoleHeading.setOrientation(LinearLayout.HORIZONTAL);
        consoleHeading.setGravity(Gravity.CENTER_VERTICAL);
        TextView consoleLabel = new TextView(activity);
        consoleLabel.setText(getLocalizedText("logs_title"));
        consoleLabel.setTextSize(9f);
        consoleLabel.setTypeface(null, Typeface.BOLD);
        consoleLabel.setTextColor(Color.parseColor("#CBD5E1"));
        consoleHeading.addView(consoleLabel, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        final Button consoleToggle = new Button(activity);
        consoleToggle.setText(getLocalizedText("show_logs"));
        consoleToggle.setTextSize(8.5f);
        applyRoundedButtonStyle(consoleToggle, Color.parseColor("#1E293B"), Color.parseColor("#7DD3FC"), 7);
        LinearLayout.LayoutParams consoleToggleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dipToPx(28));
        consoleHeading.addView(consoleToggle, consoleToggleLp);
        liveConsoleLayout.addView(consoleHeading);
        consoleToggle.setOnClickListener(new View.OnClickListener() {
            private boolean expanded = false;
            @Override public void onClick(View v) {
                expanded = !expanded;
                consoleToggle.setText(getLocalizedText(expanded ? "hide_logs" : "show_logs"));
                ViewGroup.LayoutParams consoleParams = consoleScrollView.getLayoutParams();
                consoleParams.height = dipToPx(expanded ? 150 : 42);
                consoleScrollView.setLayoutParams(consoleParams);
                if (expanded) consoleScrollView.post(new Runnable() {
                    @Override public void run() { consoleScrollView.fullScroll(View.FOCUS_DOWN); }
                });
            }
        });
        liveConsoleLayout.addView(consoleScrollView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dipToPx(42)));
        panelBody.addView(liveConsoleLayout);

        mainCard.addView(panelBody);
        panelContainer.addView(mainCard);

        layoutGroup.addView(panelContainer, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        attachedPanelHost = layoutGroup;

        populateAutoPanel();
    }

    @SimpleFunction(description = "Resets target view, all ancestor layouts and cancels running animations")
    public void ResetTargetView() {
        stopKnownExtensionAnimations();

        final View target = getSelectedTargetNativeView();
        if (target != null) {
            View current = target;
            int depth = 0;
            while (current != null && depth < 15) {
                if (!isBoltPanelView(current)) {
                    try {
                        current.clearAnimation();
                        current.animate().cancel();
                    } catch (Exception ignored) {}
                    restorePristineBackground(current);

                    current.destroyDrawingCache();
                    current.invalidate();
                }
                ViewParent parent = current.getParent();
                if (!(parent instanceof View)) break;
                current = (View) parent;
                depth++;
            }
            target.destroyDrawingCache();
            target.invalidate();
        }

        try {
            View rootDecor = activity.findViewById(android.R.id.content);
            if (rootDecor instanceof ViewGroup) {
                ViewGroup rootVg = (ViewGroup) rootDecor;
                restorePristineBackground(rootVg);
                rootVg.destroyDrawingCache();
                rootVg.invalidate();
            }
        } catch (Exception ignored) {}

        for (View v : dynamicallyAddedViews) {
            if (v != null && v.getParent() instanceof ViewGroup) {
                ((ViewGroup) v.getParent()).removeView(v);
            }
        }
        dynamicallyAddedViews.clear();

        // BUG 1 FIX: dynamicallyAddedViews मध्ये track न झालेले views देखील
        // टिकून राहत होते. त्यासाठी selected target ViewGroup मधले सर्व dynamic
        // children (BoltWS panel वगळता) थेट काढा. target चे थेट children काढल्यावर
        // त्यांचं संपूर्ण subtree सुद्धा जातं; ancestors/स्थिर nested layouts सुरक्षित राहतात.
        purgeDynamicChildrenFromHierarchy(target);

        logToConsole("[RESET] Hierarchy purged and restored.");
    }

    private void purgeDynamicChildrenFromHierarchy(View root) {
        if (!(root instanceof ViewGroup)) return;
        try {
            ViewGroup group = (ViewGroup) root;
            // BoltWS स्वतःचे panel/UI views कधीही नको काढायचे.
            if (isBoltPanelView(group)) return;
            for (int i = group.getChildCount() - 1; i >= 0; i--) {
                View child = group.getChildAt(i);
                if (child == null) continue;
                // BoltWS panel आणि Console/स्थिर controls राहू द्या.
                if (isBoltPanelView(child)) continue;
                try {
                    group.removeViewAt(i);
                } catch (Exception ignored) {}
            }
            group.invalidate();
            group.requestLayout();
        } catch (Exception ignored) {}
    }

    private boolean isViewBasedTargetType(Class<?> type) {
        return type != null && View.class.isAssignableFrom(type);
    }

    private boolean isRecyclerAdapterOrViewHolder(Class<?> type) {
        for (Class<?> current = type; current != null && current != Object.class; current = current.getSuperclass()) {
            String className = current.getName();
            if (className.endsWith("RecyclerView$Adapter") || className.endsWith("RecyclerView$ViewHolder")) {
                return true;
            }
        }
        return false;
    }

    private boolean isListContainer(View view) {
        if (view == null) return false;
        for (Class<?> current = view.getClass(); current != null; current = current.getSuperclass()) {
            String name = current.getName();
            if ("android.widget.ListView".equals(name)
                    || "androidx.recyclerview.widget.RecyclerView".equals(name)
                    || "android.support.v7.widget.RecyclerView".equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInsideListHierarchy(View view) {
        View current = view;
        while (current != null) {
            if (isListContainer(current)) return true;
            ViewParent parent = current.getParent();
            current = parent instanceof View ? (View) parent : null;
        }
        return false;
    }

    private View findListItemRoot(View target, View listContainer) {
        if (target == null || listContainer == null || target == listContainer) return null;
        View current = target;
        while (current != null) {
            ViewParent parent = current.getParent();
            if (parent == listContainer) return current;
            current = parent instanceof View ? (View) parent : null;
        }
        return null;
    }

    private int getListItemPosition(View listContainer, View itemRoot) {
        try {
            String methodName = listContainer.getClass().getName().contains("RecyclerView")
                    ? "getChildAdapterPosition" : "getPositionForView";
            Method method = listContainer.getClass().getMethod(methodName, View.class);
            Object result = method.invoke(listContainer, itemRoot);
            return result instanceof Number ? ((Number) result).intValue() : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    private long[] getStableListItemId(View listContainer, int position) {
        try {
            Method getAdapter = listContainer.getClass().getMethod("getAdapter");
            Object adapter = getAdapter.invoke(listContainer);
            if (adapter == null) return new long[0];
            Method hasStableIds = adapter.getClass().getMethod("hasStableIds");
            Object stable = hasStableIds.invoke(adapter);
            if (!(stable instanceof Boolean) || !((Boolean) stable)) return new long[0];
            Method getItemId = adapter.getClass().getMethod("getItemId", int.class);
            Object id = getItemId.invoke(adapter, position);
            return id instanceof Number ? new long[]{((Number) id).longValue()} : new long[0];
        } catch (Exception e) {
            return new long[0];
        }
    }

    private TargetViewSnapshot createTargetViewSnapshot(View target) {
        List<View> ancestry = new ArrayList<View>();
        View current = target;
        while (current != null) {
            ancestry.add(current);
            ViewParent parent = current.getParent();
            current = parent instanceof View ? (View) parent : null;
        }

        View listContainer = null;
        current = target;
        while (current != null) {
            if (isListContainer(current)) {
                listContainer = current;
                break;
            }
            ViewParent parent = current.getParent();
            current = parent instanceof View ? (View) parent : null;
        }

        View itemRoot = findListItemRoot(target, listContainer);
        int itemPosition = itemRoot != null ? getListItemPosition(listContainer, itemRoot) : -1;
        long[] stableId = itemRoot != null && itemPosition >= 0
                ? getStableListItemId(listContainer, itemPosition) : new long[0];
        return new TargetViewSnapshot(ancestry, listContainer, itemRoot, itemPosition,
                stableId.length > 0, stableId.length > 0 ? stableId[0] : 0L);
    }

    private void addAvailableTargetView(View view, String name) {
        if (view == null) return;
        availableTargetViews.add(view);
        availableTargetNames.add(name);
        targetViewSnapshots.put(view, createTargetViewSnapshot(view));
    }

    private boolean isTargetViewStillValid(View target) {
        if (target == null) return false;
        TargetViewSnapshot snapshot = targetViewSnapshots.get(target);
        if (snapshot == null || target.getWindowToken() == null) return false;

        View contentRoot = activity != null ? activity.findViewById(android.R.id.content) : null;
        boolean belongsToCurrentHierarchy = false;
        View current = target;
        int ancestryIndex = 0;
        while (current != null) {
            if (ancestryIndex >= snapshot.ancestry.size() || snapshot.ancestry.get(ancestryIndex) != current) {
                return false;
            }
            if (current == contentRoot) belongsToCurrentHierarchy = true;
            ancestryIndex++;
            ViewParent parent = current.getParent();
            current = parent instanceof View ? (View) parent : null;
        }
        if (!belongsToCurrentHierarchy || ancestryIndex != snapshot.ancestry.size()) return false;

        if (snapshot.listItemRoot != null) {
            View currentListContainer = null;
            current = target;
            while (current != null) {
                if (isListContainer(current)) {
                    currentListContainer = current;
                    break;
                }
                ViewParent parent = current.getParent();
                current = parent instanceof View ? (View) parent : null;
            }
            View currentItemRoot = findListItemRoot(target, currentListContainer);
            if (currentListContainer != snapshot.listContainer || currentItemRoot != snapshot.listItemRoot) return false;
            int currentPosition = getListItemPosition(currentListContainer, currentItemRoot);
            if (currentPosition < 0 || currentPosition != snapshot.itemPosition) return false;
            if (snapshot.hasStableItemId) {
                long[] currentStableId = getStableListItemId(currentListContainer, currentPosition);
                if (currentStableId.length == 0 || currentStableId[0] != snapshot.stableItemId) return false;
            }
        }
        return true;
    }

    private void autoSuggestBestTargetView(Method m) {
        if (userManuallySelectedTarget) return;
        if (m == null || targetViewSpinner == null || availableTargetViews.isEmpty()) return;

        Class<?>[] pTypes = m.getParameterTypes();
        if (pTypes.length > 0 && isViewBasedTargetType(pTypes[0])) {
            int selectedPosition = targetViewSpinner.getSelectedItemPosition();
            if (pTypes[0] == View.class && selectedPosition >= 0 && selectedPosition < availableTargetViews.size()
                    && pTypes[0].isInstance(availableTargetViews.get(selectedPosition))) {
                return;
            }
            int exactIndex = -1;
            int compatibleIndex = -1;
            int compatibleScore = Integer.MAX_VALUE;
            for (int i = 0; i < availableTargetViews.size(); i++) {
                View candidate = availableTargetViews.get(i);
                if (candidate == null) continue;
                if (candidate.getClass() == pTypes[0]) {
                    exactIndex = i;
                    break;
                }
                int score = getTargetTypeMatchScore(pTypes[0], candidate);
                if (score >= 0 && score < compatibleScore) {
                    compatibleScore = score;
                    compatibleIndex = i;
                }
            }
            int bestIndex = exactIndex >= 0 ? exactIndex : compatibleIndex;
            if (bestIndex >= 0 && targetViewSpinner.getSelectedItemPosition() != bestIndex) {
                targetViewSpinner.setSelection(bestIndex);
                String viewName = bestIndex < availableTargetNames.size() ? availableTargetNames.get(bestIndex) : "Target View";
                logToConsole("[TARGET-VIEW] Auto: " + viewName);
            }
            return;
        }

        String mNameLower = m.getName().toLowerCase(Locale.ROOT);

        int bestIndex = -1;
        boolean wantsTextView = mNameLower.contains("text") || mNameLower.contains("font") || mNameLower.contains("shadow") || mNameLower.contains("letter");
        boolean wantsCardOrLayout = mNameLower.contains("layout") || mNameLower.contains("arrangement") || mNameLower.contains("card") || mNameLower.contains("neumorph") || mNameLower.contains("border") || mNameLower.contains("target");

        if (pTypes.length > 0) {
            Class<?> p0 = pTypes[0];
            if (ViewGroup.class.isAssignableFrom(p0) || p0.getSimpleName().contains("Arrangement") || p0.getSimpleName().contains("Card")) {
                wantsCardOrLayout = true;
            } else if (TextView.class.isAssignableFrom(p0) || p0.getSimpleName().contains("Label") || p0.getSimpleName().contains("TextBox")) {
                wantsTextView = true;
            }
        }

        for (int i = 0; i < availableTargetViews.size(); i++) {
            View v = availableTargetViews.get(i);
            if (wantsTextView && (v instanceof TextView)) {
                bestIndex = i;
                break;
            }
            if (wantsCardOrLayout && (v instanceof ViewGroup) && !(v instanceof TextView)) {
                bestIndex = i;
                break;
            }
        }

        if (bestIndex != -1 && targetViewSpinner.getSelectedItemPosition() != bestIndex) {
            targetViewSpinner.setSelection(bestIndex);
            String viewName = (bestIndex < availableTargetNames.size()) ? availableTargetNames.get(bestIndex) : "Target View";
            logToConsole("[TARGET-VIEW] Auto: " + viewName);
        }
    }

    private int getTargetTypeMatchScore(Class<?> expectedType, View candidate) {
        if (expectedType == null || candidate == null || !expectedType.isInstance(candidate)) return -1;
        if (candidate.getClass() == expectedType) return 0;
        int distance = 1;
        for (Class<?> current = candidate.getClass().getSuperclass(); current != null; current = current.getSuperclass()) {
            if (current == expectedType) return distance;
            distance++;
        }
        return 100;
    }

    private Object getSelectedTargetView(Class<?> expectedType) {
        View selected = null;
        if (targetViewSpinner != null && targetViewSpinner.getSelectedItemPosition() >= 0
                && targetViewSpinner.getSelectedItemPosition() < availableTargetViews.size()) {
            selected = availableTargetViews.get(targetViewSpinner.getSelectedItemPosition());
        }
        if (selected == null) {
            selected = defaultTargetComponent != null ? defaultTargetComponent.getView() : null;
        }
        if (selected == null) {
            selected = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        }

        if (isViewBasedTargetType(expectedType)) {
            if (selected == null || !expectedType.isInstance(selected)) {
                throw new IllegalArgumentException("Target type mismatch. Expected "
                        + expectedType.getSimpleName() + " but selected "
                        + (selected == null ? "none" : selected.getClass().getSimpleName()) + ".");
            }
            if (!isInsideListHierarchy(selected)) applyCenterTargetLayout(selected);
            return selected;
        }

        applyCenterTargetLayout(selected);
        if (AndroidViewComponent.class.isAssignableFrom(expectedType) || Component.class.isAssignableFrom(expectedType)) {
            final View finalSelectedView = selected;

            final ComponentContainer dynamicContainer = (ComponentContainer) Proxy.newProxyInstance(
                    ComponentContainer.class.getClassLoader(),
                    new Class<?>[]{ComponentContainer.class},
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            String mName = method.getName();
                            if ("$context".equals(mName)) return activity;
                            if ("$form".equals(mName)) return formInstance;
                            if ("$add".equals(mName) && args != null && args.length > 0) {
                                if (args[0] instanceof AndroidViewComponent) {
                                    View v = ((AndroidViewComponent) args[0]).getView();
                                    if (v != null) {
                                        ViewGroup root = (finalSelectedView instanceof ViewGroup) ? (ViewGroup) finalSelectedView
                                                : (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
                                        if (v.getParent() instanceof ViewGroup) {
                                            ((ViewGroup) v.getParent()).removeView(v);
                                        }
                                        applyCenterTargetLayout(v);
                                        root.addView(v);
                                        dynamicallyAddedViews.add(v);
                                        v.bringToFront();
                                        v.requestLayout();
                                        v.invalidate();
                                    }
                                }
                                return null;
                            }
                            if ("Width".equals(mName)) return finalSelectedView != null ? finalSelectedView.getWidth() : dipToPx(300);
                            if ("Height".equals(mName)) return finalSelectedView != null ? finalSelectedView.getHeight() : dipToPx(220);
                            if (formInstance != null) {
                                try { return method.invoke(formInstance, args); } catch (Throwable ignored) {}
                            }
                            return null;
                        }
                    }
            );

            try {
                Constructor<?> ctor = expectedType.getConstructor(ComponentContainer.class);
                Object compInstance = ctor.newInstance(dynamicContainer);
                try {
                    Field vf = expectedType.getDeclaredField("view");
                    vf.setAccessible(true);
                    vf.set(compInstance, finalSelectedView);
                } catch (Throwable ignored) {}
                return compInstance;
            } catch (Throwable ignored) {}

            return new AndroidViewComponent(dynamicContainer) {
                @Override
                public View getView() {
                    return finalSelectedView;
                }
            };
        }

        return selected;
    }

    private boolean validateMethodTarget(Method method) {
        if (method == null) return false;
        Class<?>[] types = method.getParameterTypes();
        for (Class<?> type : types) {
            if (isRecyclerAdapterOrViewHolder(type)) {
                String message = "The generic target tester cannot safely provide "
                        + type.getSimpleName() + ". No Adapter or ViewHolder will be created.";
                recordGuideExecutionFailure(method.getName(), message);
                logToConsole("[TARGET-ERR] " + message);
                return false;
            }
        }
        if (types.length == 0 || !isViewBasedTargetType(types[0])) return true;

        View selected = getSelectedTargetNativeView();
        if (selected == null || !types[0].isInstance(selected)) {
            String expected = types[0].getSimpleName();
            String actual = selected == null ? "none" : selected.getClass().getSimpleName();
            String message = "Target type mismatch\nExpected: " + expected + "\nSelected: " + actual
                    + "\n\nSolution:\nSelect the actual " + expected + " target.";
            recordGuideExecutionFailure(method.getName(), message);
            logToConsole("[TARGET-ERR] " + message);
            return false;
        }
        if (!isTargetViewStillValid(selected)) {
            String message = "Target is no longer valid.\nThe list may have been scrolled or rebound.\n\n"
                    + "Please rescan/select the target again.";
            recordGuideExecutionFailure(method.getName(), message);
            logToConsole("[TARGET-ERR] " + message);
            return false;
        }
        return true;
    }

    private void executeSelectedMethodFromUI() {
        if (currentExtensionClass == null || methodSpinner == null) return;
        final Method targetMethod = getSelectedMethodEntry();
        if (targetMethod == null) {
            logToConsole("[ERR] Please select a method.");
            return;
        }
        final String methodName = targetMethod.getName();
        if (!validateSelectedPropertySources(targetMethod)) return;

        final String rawArgs = argsEditText != null ? argsEditText.getText().toString().trim() : "";
        final List<String> splitArgs;
        try {
            splitArgs = parseArgumentTokens(rawArgs);
        } catch (IllegalArgumentException e) {
            recordGuideExecutionFailure(methodName, e.getMessage());
            logToConsole("[INPUT-ERR] " + e.getMessage());
            return;
        }
        if (!validateMethodTarget(targetMethod)) return;

        Class<?>[] paramTypes = targetMethod.getParameterTypes();
        int reqUserParams = 0;
        for (int i = 0; i < paramTypes.length; i++) {
            if (!(i == 0 && (AndroidViewComponent.class.isAssignableFrom(paramTypes[i]) || View.class.isAssignableFrom(paramTypes[i])))) {
                reqUserParams++;
            }
        }

        if (splitArgs.size() != reqUserParams) {
            String message = "Expected " + reqUserParams + " argument(s) for "
                    + formatMethodIdentity(targetMethod) + ", found " + splitArgs.size() + ".";
            recordGuideExecutionFailure(methodName, message);
            logToConsole("[INPUT-ERR] " + message);
            return;
        }

        View target = getSelectedTargetNativeView();
        capturePristineBaseline(target);
        if (!isInsideListHierarchy(target)) applyCenterTargetLayout(target);

        String cacheKey = getMethodArgumentsCacheKey(targetMethod);
        try {
            SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(cacheKey, rawArgs).apply();
            argumentsCache.put(cacheKey, rawArgs);
        } catch (Exception ignored) {}

        if (paramTypes.length == 0 && targetMethod.getReturnType() != void.class) {
            try {
                final Object getterResult = targetMethod.invoke(currentExtensionInstance);
                cacheCustomObjectResult(targetMethod, getterResult);
                final String strResult = String.valueOf(getterResult);
                logToConsole("[GETTER] " + methodName + " -> " + strResult);
                MethodResultReceived(methodName, strResult);
            } catch (Exception e) {
                recordGuideExecutionFailure(methodName, e.getMessage());
                logToConsole("[GETTER-ERR] " + e.getMessage());
            }
            return;
        }

        scheduleRenderPipeline(targetMethod, rawArgs);
    }

    public Method findMethodByName(String methodName) {
        if (currentExtensionClass == null || methodName == null) return null;
        Method fallback = null;
        for (Method m : currentExtensionClass.getMethods()) {
            if (m.getName().equalsIgnoreCase(methodName)) {
                if (m.getParameterTypes().length > 0) return m;
                if (fallback == null) fallback = m;
            }
        }
        return fallback;
    }

    private Method getSelectedMethodEntry() {
        int position = methodSpinner != null ? methodSpinner.getSelectedItemPosition() : -1;
        if (position >= 0 && position < currentLoadedMethodEntries.size()) {
            return currentLoadedMethodEntries.get(position);
        }
        return null;
    }

    private String formatMethodIdentity(Method method) {
        StringBuilder identity = new StringBuilder(method.getName()).append('(');
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) identity.append(", ");
            identity.append(types[i].getName());
        }
        return identity.append(") -> ").append(method.getReturnType().getName()).toString();
    }

    private String getMethodArgumentsCacheKey(Method method) {
        return (currentClassName != null ? currentClassName : "Bolt") + ":"
                + method.getDeclaringClass().getName() + "#" + formatMethodIdentity(method);
    }

    private Method findMethodByNameAndArgs(String methodName, int argCount) {
        lastMethodResolutionError = "";
        if (currentExtensionClass == null || methodName == null) {
            lastMethodResolutionError = "No extension class or method name is available.";
            return null;
        }
        List<Method> candidates = new ArrayList<Method>();
        View selectedTarget = getSelectedTargetNativeView();
        for (Method m : currentExtensionClass.getMethods()) {
            if (!m.getName().equals(methodName) || m.isBridge() || m.isSynthetic()) continue;
            Class<?>[] types = m.getParameterTypes();
            int targetOffset = types.length > 0 && (AndroidViewComponent.class.isAssignableFrom(types[0])
                    || View.class.isAssignableFrom(types[0])) ? 1 : 0;
            if (types.length - targetOffset == argCount) {
                boolean duplicate = false;
                for (Method candidate : candidates) {
                    if (formatMethodIdentity(candidate).equals(formatMethodIdentity(m))
                            && candidate.getDeclaringClass().equals(m.getDeclaringClass())) {
                        duplicate = true;
                        break;
                    }
                }
                if (!duplicate) candidates.add(m);
            }
        }

        if (candidates.isEmpty()) {
            lastMethodResolutionError = "No overload of " + methodName + " accepts " + argCount + " argument(s).";
            return null;
        }
        if (candidates.size() == 1) return candidates.get(0);

        Method bestTargetMatch = null;
        int bestTargetScore = Integer.MAX_VALUE;
        boolean tied = false;
        boolean allCandidatesAreViewTargetOverloads = true;
        for (Method candidate : candidates) {
            Class<?>[] types = candidate.getParameterTypes();
            if (types.length == 0 || !View.class.isAssignableFrom(types[0])) {
                allCandidatesAreViewTargetOverloads = false;
                continue;
            }
            int score = getTargetTypeMatchScore(types[0], selectedTarget);
            if (score >= 0 && score < bestTargetScore) {
                bestTargetMatch = candidate;
                bestTargetScore = score;
                tied = false;
            } else if (score >= 0 && score == bestTargetScore) {
                tied = true;
            }
        }
        if (allCandidatesAreViewTargetOverloads && bestTargetMatch != null && !tied) return bestTargetMatch;

        StringBuilder choices = new StringBuilder();
        for (Method candidate : candidates) {
            if (choices.length() > 0) choices.append("; ");
            choices.append(formatMethodIdentity(candidate));
        }
        lastMethodResolutionError = "Ambiguous method " + methodName + ": " + choices
                + ". Select an exact overload.";
        return null;
    }

    private void scheduleRenderPipeline(final Method targetMethod, final String argsStr) {
        final String methodName = targetMethod.getName();
        final long generation = globalRenderGeneration.incrementAndGet();

        if (pendingRenderRunnable != null) {
            mainHandler.removeCallbacks(pendingRenderRunnable);
            pendingRenderRunnable = null;
        }

        if (!isInsideListHierarchy(getSelectedTargetNativeView())) {
            stopKnownExtensionAnimations();
        }

        pendingRenderRunnable = new Runnable() {
            @Override
            public void run() {
                if (generation != globalRenderGeneration.get()) return;
                pendingRenderRunnable = null;

                logToConsole("[RENDER] " + methodName + "(" + argsStr + ")");

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (generation != globalRenderGeneration.get()) return;
                            if (!validateMethodTarget(targetMethod)) return;
                            runMethodWithArgs(targetMethod, targetMethod.getParameterTypes(), argsStr);
                        } catch (Throwable t) {
                            recordSavedProjectTest(methodName, argsStr, "FAILURE", getProviderHistorySnapshot(), null,
                                    t.getClass().getSimpleName() + ": " + t.getMessage());
                            inspectAndLogExtensionError(t, methodName, argsStr);
                        }
                    }
                });
            }
        };

        mainHandler.postDelayed(pendingRenderRunnable, RENDER_FLUSH_DELAY_MS);
    }

    public void stopKnownExtensionAnimations() {
        View target = getSelectedTargetNativeView();
        if (isInsideListHierarchy(target)) return;
        try {
            View current = target;
            int depth = 0;
            while (current != null && depth < 15) {
                if (!isBoltPanelView(current)) {
                    current.clearAnimation();
                    current.animate().cancel();
                }
                ViewParent parent = current.getParent();
                if (!(parent instanceof View)) break;
                current = (View) parent;
                depth++;
            }
        } catch (Exception ignored) {}

        if (currentExtensionInstance == null || currentExtensionClass == null) return;

        try {
            for (Field f : currentExtensionClass.getDeclaredFields()) {
                f.setAccessible(true);
                Object val = f.get(currentExtensionInstance);
                if (val instanceof android.animation.Animator) {
                    android.animation.Animator a = (android.animation.Animator) val;
                    a.removeAllListeners();
                    a.cancel();
                    f.set(currentExtensionInstance, null);
                }
            }
        } catch (Exception ignored) {}

        String[] candidates = new String[] {
                "StopAnimation", "StopAnimations", "CancelAnimation",
                "CancelAnimations", "StopPulse", "StopRipple",
                "ClearAnimation", "ClearAnimations", "ResetView"
        };

        for (String name : candidates) {
            try {
                Method m = currentExtensionClass.getMethod(name);
                if (m.getParameterTypes().length == 0) {
                    m.invoke(currentExtensionInstance);
                }
            } catch (Exception ignored) {}
        }
    }

    public View getSelectedTargetNativeView() {
        if (targetViewSpinner != null) {
            int pos = targetViewSpinner.getSelectedItemPosition();
            if (pos >= 0 && pos < availableTargetViews.size()) {
                return availableTargetViews.get(pos);
            }
        }
        return defaultTargetComponent != null ? defaultTargetComponent.getView() : null;
    }

    private void inspectAndLogExtensionError(Throwable t, String methodName, String argsStr) {
        Throwable root = t;
        if (root instanceof java.lang.reflect.InvocationTargetException && ((java.lang.reflect.InvocationTargetException) root).getTargetException() != null) {
            root = ((java.lang.reflect.InvocationTargetException) root).getTargetException();
        }

        String errType = root.getClass().getSimpleName();
        String errMsg = root.getMessage() != null ? root.getMessage() : "No details";
        recordGuideExecutionFailure(methodName, errType + ": " + errMsg);

        logToConsole("");
        logToConsole("[CRASH DETECTED] " + errType + ": " + errMsg);
        logToConsole("");
    }

    private void recordGuideExecutionFailure(String methodName, String message) {
        lastExecutionStatus = "ERROR";
        lastExecutionMethod = methodName != null ? methodName : "";
        lastExecutionMessage = message != null ? message : "Execution failed.";
        lastExecutionResult = "";
        lastErrorMessage = lastExecutionMessage;
        if (!currentErrorMessage.isEmpty()) previousErrorMessage = currentErrorMessage;
        currentErrorMessage = lastExecutionMessage;
    }

    private boolean isCacheableCustomObject(Object value) {
        return value != null
                && !(value instanceof String)
                && !(value instanceof Number)
                && !(value instanceof Boolean)
                && !(value instanceof Character)
                && !(value instanceof View)
                && !(value instanceof AndroidViewComponent)
                && !(value instanceof com.google.appinventor.components.runtime.util.YailList)
                && !(value instanceof java.util.Collection)
                && !(value instanceof java.util.Map)
                && !value.getClass().isArray();
    }

    private void cacheCustomObjectResult(Method method, Object value) {
        if (!isCacheableCustomObject(value)) return;
        customObjectResults.put(method.getName(), value);
        logToConsole("[OBJECT-CACHE] " + method.getName()
                + " -> " + value.getClass().getName());
    }

    private void runMethodWithArgs(Method targetMethod, Class<?>[] paramTypes, String rawArgs) throws Exception {
        runMethodWithArgs(targetMethod, paramTypes, rawArgs, null);
    }

    private void runMethodWithArgs(Method targetMethod, Class<?>[] paramTypes, String rawArgs,
                                  Map<Integer, Object> argumentOverrides) throws Exception {
        if (!isCurrentExtensionMethod(targetMethod)) {
            throw new IllegalStateException("Selected method belongs to a stale DEX/class loader. Select it again.");
        }
        if (!validateMethodTarget(targetMethod)) return;
        final View selectedTarget = getSelectedTargetNativeView();
        final boolean listRelatedTarget = isInsideListHierarchy(selectedTarget);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (View v : dynamicallyAddedViews) {
                        if (v != null && v.getParent() instanceof ViewGroup) {
                            ((ViewGroup) v.getParent()).removeView(v);
                        }
                    }
                    dynamicallyAddedViews.clear();
                } catch (Exception ignored) {}
            }
        });

        List<Object> arguments = new ArrayList<Object>();
        int argIndex = 0;
        List<String> splitArgs = parseArgumentTokens(rawArgs);

        int expectedArguments = paramTypes.length;
        if (paramTypes.length > 0 && (AndroidViewComponent.class.isAssignableFrom(paramTypes[0])
                || View.class.isAssignableFrom(paramTypes[0]))) {
            expectedArguments--;
        }
        if (splitArgs.size() != expectedArguments) {
            throw new IllegalArgumentException("Expected " + expectedArguments + " literal argument(s), found "
                    + splitArgs.size() + ".");
        }

        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> pType = paramTypes[i];
            if (i == 0 && (AndroidViewComponent.class.isAssignableFrom(pType) || View.class.isAssignableFrom(pType))) {
                arguments.add(getSelectedTargetView(pType));
                continue;
            }

            if (argIndex < splitArgs.size()) {
                if (argumentOverrides != null && argumentOverrides.containsKey(argIndex)) {
                    arguments.add(convertRuntimeArgument(argumentOverrides.get(argIndex), pType));
                    argIndex++;
                    continue;
                }
                String propertyName = selectedGetterProperties.get(
                        getPropertySelectionKey(targetMethod, argIndex));
                if (propertyName == null) {
                    String rawValue = splitArgs.get(argIndex).trim();

                    if (rawValue.startsWith("$")) {
                        String objectKey = rawValue.substring(1).trim();
                        Object cachedObject = customObjectResults.get(objectKey);
                        if (cachedObject == null) {
                            throw new IllegalArgumentException(
                                    "Custom object not found: $" + objectKey
                                    + ". Run the configuration block first.");
                        }
                        if (!pType.isInstance(cachedObject)) {
                            throw new IllegalArgumentException(
                                    "Cached object $" + objectKey + " has type "
                                    + cachedObject.getClass().getName()
                                    + ", incompatible with " + pType.getName());
                        }
                        arguments.add(cachedObject);
                    } else {
                    Object resolvedValue = null;
                    boolean getterResolved = false;

                    // Resolve an exact zero-argument getter name only when the
                    // target parameter is numeric and the current DEX exposes
                    // a unique compatible method with that exact name.
                    if (isNumericType(pType) && currentExtensionInstance != null) {
                        Method getterCandidate = null;
                        for (Method candidate : currentExtensionInstance.getClass().getMethods()) {
                            if (candidate.getName().equals(rawValue)
                                    && candidate.getParameterTypes().length == 0
                                    && candidate.getReturnType() != void.class
                                    && !candidate.isBridge()
                                    && !candidate.isSynthetic()
                                    && isBlockParameterTypeCompatible(
                                            candidate.getReturnType().getName(), pType)) {
                                if (getterCandidate != null
                                        && !getterCandidate.equals(candidate)) {
                                    throw new IllegalArgumentException(
                                            "Ambiguous Getter reference: " + rawValue);
                                }
                                getterCandidate = candidate;
                            }
                        }

                        if (getterCandidate != null) {
                            try {
                                resolvedValue = getterCandidate.invoke(currentExtensionInstance);
                            } catch (java.lang.reflect.InvocationTargetException e) {
                                Throwable cause = e.getTargetException() != null
                                        ? e.getTargetException() : e;
                                throw new IllegalStateException(
                                        "Getter " + rawValue + " failed: "
                                                + (cause.getMessage() != null
                                                ? cause.getMessage()
                                                : cause.getClass().getSimpleName()), cause);
                            }
                            getterResolved = true;
                            logToConsole("[GETTER-RESOLVED] " + rawValue
                                    + " -> parameter " + (argIndex + 1)
                                    + ", ReturnType=" + getterCandidate.getReturnType().getName()
                                    + ", Value=" + String.valueOf(resolvedValue));
                        }
                    }

                    arguments.add(getterResolved
                            ? convertRuntimeArgument(resolvedValue, pType)
                            : convertMethodArgument(rawValue, targetMethod, i, pType));
                    }
                } else {
                    arguments.add(readPropertyForArgument(
                            targetMethod, argIndex, propertyName, pType));
                }
                argIndex++;
            }
        }

        if (!isCurrentExtensionMethod(targetMethod)) {
            throw new IllegalStateException("Extension instance changed before invocation; retry with the current DEX.");
        }
        BlockPropertyMetadata setterProperty = findVerifiedPropertySetter(targetMethod);
        Object methodResult = targetMethod.invoke(currentExtensionInstance, arguments.toArray());
        cacheCustomObjectResult(targetMethod, methodResult);
        recordSavedProjectTest(targetMethod.getName(), rawArgs, "SUCCESS", getProviderHistorySnapshot(),
                targetMethod.getReturnType() == void.class || methodResult == null ? null : String.valueOf(methodResult), null);

        if (setterProperty != null) {
            logToConsole("[PROPERTY-SETTER] " + setterProperty.name + " (" + setterProperty.type
                    + ") applied to the loaded extension instance.");
        } else {
            logToConsole("[EXEC] " + targetMethod.getName() + "() applied successfully.");
        }
        MethodExecuted(targetMethod.getName());

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (listRelatedTarget) return;
                try {
                    View tView = selectedTarget;
                    if (tView != null) {
                        applyCenterTargetLayout(tView);
                        tView.setVisibility(View.VISIBLE);
                        ViewParent parent = tView.getParent();
                        while (parent instanceof ViewGroup) {
                            ViewGroup parentVg = (ViewGroup) parent;
                            parentVg.setClipChildren(false);
                            parentVg.setClipToPadding(false);
                            parentVg.invalidate();
                            parentVg.requestLayout();
                            parent = parent.getParent();
                        }
                        tView.bringToFront();
                        tView.invalidate();
                        tView.requestLayout();
                    }
                } catch (Exception ignored) {}
            }
        });
    }


    /**
     * Resolve a component property using the running Form's public runtime API.
     *
     * Accepted reference syntax: ComponentName.PropertyName
     * This deliberately refuses to guess when the Form cannot resolve a component.
     */
    private Object resolveExternalComponentProperty(String reference,
                                                    Class<?> parameterType,
                                                    int argumentIndex) throws Exception {
        if (reference == null) {
            throw new IllegalArgumentException("Getter reference is null.");
        }

        int dot = reference.lastIndexOf('.');
        if (dot <= 0 || dot == reference.length() - 1) {
            throw new IllegalArgumentException(
                    "External Getter must use ComponentName.PropertyName syntax.");
        }

        final String componentName = reference.substring(0, dot).trim();
        final String propertyName = reference.substring(dot + 1).trim();

        if (componentName.isEmpty() || propertyName.isEmpty()) {
            throw new IllegalArgumentException("Component or Property name is empty.");
        }
        if (formInstance == null) {
            throw new IllegalStateException("Running Form is not available.");
        }

        Object component = null;
        Method resolver = null;

        // Only use a real public resolver exposed by this App Inventor/Kodular runtime.
        for (Method candidate : formInstance.getClass().getMethods()) {
            if (!candidate.getName().equals("getComponentByName")
                    || candidate.getParameterTypes().length != 1
                    || candidate.getParameterTypes()[0] != String.class
                    || candidate.getReturnType() == void.class) {
                continue;
            }
            if (resolver != null) {
                throw new IllegalStateException(
                        "Form exposes ambiguous getComponentByName(String) methods.");
            }
            resolver = candidate;
        }

        if (resolver == null) {
            throw new UnsupportedOperationException(
                    "This runtime does not expose Form.getComponentByName(String); "
                    + "external Component Getters cannot be resolved safely.");
        }

        try {
            component = resolver.invoke(formInstance, componentName);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getTargetException() != null ? e.getTargetException() : e;
            throw new IllegalStateException(
                    "Component lookup failed for " + componentName + ": "
                    + cause.getMessage(), cause);
        }

        if (component == null) {
            throw new IllegalArgumentException(
                    "Component not found in the running Form: " + componentName);
        }

        Method getter = null;
        String[] getterNames = new String[] {
                propertyName,
                "get" + propertyName,
                "is" + propertyName
        };

        for (String getterName : getterNames) {
            for (Method candidate : component.getClass().getMethods()) {
                if (!candidate.getName().equals(getterName)
                        || candidate.getParameterTypes().length != 0
                        || candidate.getReturnType() == void.class
                        || candidate.isBridge()
                        || candidate.isSynthetic()) {
                    continue;
                }

                if (getter != null && !getter.equals(candidate)) {
                    throw new IllegalArgumentException(
                            "Ambiguous Getter for " + reference);
                }
                getter = candidate;
            }
            if (getter != null) break;
        }

        if (getter == null) {
            throw new IllegalArgumentException(
                    "No public zero-argument Getter found for " + reference);
        }

        final Object value;
        try {
            value = getter.invoke(component);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getTargetException() != null ? e.getTargetException() : e;
            throw new IllegalStateException(
                    "Getter failed for " + reference + ": " + cause.getMessage(), cause);
        }

        try {
            Object converted = convertRuntimeArgument(value, parameterType);
            logToConsole("[GETTER-RESOLVED] " + reference
                    + " -> parameter " + (argumentIndex + 1)
                    + " (" + parameterType.getSimpleName() + ")");
            return converted;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Runtime value from " + reference + " cannot be passed to parameter "
                    + (argumentIndex + 1) + " (" + parameterType.getName() + "): "
                    + e.getMessage(), e);
        }
    }

    private Object readPropertyForArgument(Method method, int argumentIndex,
                                          String propertyName, Class<?> parameterType) throws Exception {
        if (!isCurrentExtensionMethod(method)) {
            throw new IllegalStateException("Selected method belongs to a stale extension instance.");
        }
        // A dotted reference denotes a running project's component/property.
        // A plain name continues to use the existing verified extension Getter path.
        if (propertyName != null && propertyName.indexOf('.') > 0) {
            return resolveExternalComponentProperty(
                    propertyName, parameterType, argumentIndex);
        }

        BlockPropertyMetadata property = findBlockProperty(propertyName);
        Method getter = findPropertyAccessor(property, true);

        // Fallback for zero-argument getters on the current extension instance.
        if (getter == null && property == null && currentExtensionInstance != null) {
            Method candidate = null;
            for (Method m : currentExtensionInstance.getClass().getMethods()) {
                if (!m.getName().equals(propertyName)
                        || m.getParameterTypes().length != 0
                        || m.getReturnType() == void.class
                        || m.isBridge()
                        || m.isSynthetic()
                        || !isBlockParameterTypeCompatible(m.getReturnType().getName(), parameterType)) {
                    continue;
                }
                if (candidate != null && !candidate.equals(m)) {
                    throw new IllegalArgumentException(
                            "Ambiguous zero-argument Getter: " + propertyName);
                }
                candidate = m;
            }
            getter = candidate;
        }

        if (getter == null
                || (property != null
                    && !isBlockParameterTypeCompatible(property.type, parameterType))) {
            throw new IllegalArgumentException("Getter " + propertyName
                    + " has no unique compatible accessor for "
                    + parameterType.getName() + ".");
        }

        final Object value;
        try {
            value = getter.invoke(currentExtensionInstance);
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getTargetException() != null ? e.getTargetException() : e;
            throw new IllegalStateException("Getter " + formatMethodIdentity(getter)
                    + " failed: " + (cause.getMessage() != null ? cause.getMessage()
                    : cause.getClass().getSimpleName()), cause);
        }
        try {
            return convertRuntimeArgument(value, parameterType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Getter " + propertyName + " value cannot be converted to "
                    + parameterType.getName() + " for parameter " + (argumentIndex + 1)
                    + ": " + e.getMessage(), e);
        }
    }

    private Object convertRuntimeArgument(Object value, Class<?> targetType) {
        if (value == null) return convertArgument(null, targetType);
        if (targetType == Object.class || targetType.isInstance(value)) return value;
        if (value instanceof Number && isNumericType(targetType)) {
            return convertArgument(value.toString(), targetType);
        }
        if (value instanceof String || value instanceof Boolean) {
            return convertArgument(String.valueOf(value), targetType);
        }
        throw new IllegalArgumentException("Runtime value type " + value.getClass().getName()
                + " cannot be safely converted to " + targetType.getName() + ".");
    }

    private boolean isNumericType(Class<?> type) {
        return type == byte.class || type == Byte.class
                || type == short.class || type == Short.class
                || type == int.class || type == Integer.class
                || type == long.class || type == Long.class
                || type == float.class || type == Float.class
                || type == double.class || type == Double.class;
    }

    private Object convertArgument(Object val, Class<?> targetType) {
        if (val == null) {
            if (!targetType.isPrimitive()) return null;
            throw new IllegalArgumentException("A value is required for " + targetType.getSimpleName() + ".");
        }
        if (val instanceof AndroidViewComponent) {
            View nativeView = ((AndroidViewComponent) val).getView();
            if (targetType.isInstance(val)) return val;
            if (nativeView != null && targetType.isInstance(nativeView)) return nativeView;
            throw new IllegalArgumentException("Component value is not compatible with " + targetType.getSimpleName() + ".");
        }
        if (!val.getClass().equals(String.class)) {
            if (targetType.isInstance(val)) return val;
            throw new IllegalArgumentException("Value type " + val.getClass().getSimpleName()
                    + " is not compatible with " + targetType.getSimpleName() + ".");
        }
        String raw = String.valueOf(val).trim();

        if (targetType == YailList.class || List.class.isAssignableFrom(targetType)) {
            List<Object> listItems = new ArrayList<Object>();
            if (raw.startsWith("[") && raw.endsWith("]")) {
                try {
                    JSONArray jsonArray = new JSONArray(raw);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        listItems.add(normalizeJsonValue(jsonArray.get(i)));
                    }
                } catch (Exception notJson) {
                    List<String> items = splitDelimitedValues(raw.substring(1, raw.length() - 1), true);
                    for (String item : items) {
                        if (!item.isEmpty()) listItems.add(parseListItemSafely(item));
                    }
                }
            } else {
                List<String> items = splitDelimitedValues(raw, true);
                for (String item : items) {
                    if (!item.isEmpty()) listItems.add(parseListItemSafely(item));
                }
            }
            if (targetType == YailList.class) return YailList.makeList(listItems);
            if (targetType.isInterface() || targetType.isAssignableFrom(ArrayList.class)) return listItems;
            throw new IllegalArgumentException("Cannot safely construct " + targetType.getSimpleName() + ".");
        }

        if (targetType == YailDictionary.class || Map.class.isAssignableFrom(targetType)) {
            try {
                JSONObject json = new JSONObject(raw);
                Map<Object, Object> map = new HashMap<Object, Object>();
                java.util.Iterator<String> keys = json.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    map.put(k, normalizeJsonValue(json.get(k)));
                }
                if (targetType == YailDictionary.class) return YailDictionary.makeDictionary(map);
                if (targetType.isInterface() || targetType.isAssignableFrom(HashMap.class)) return map;
                throw new IllegalArgumentException("Cannot safely construct " + targetType.getSimpleName() + ".");
            } catch (IllegalArgumentException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid JSON object for " + targetType.getSimpleName() + ".", e);
            }
        }

        String str = targetType == String.class ? decodeQuotedArgument(raw) : raw;
        if (targetType == Object.class) return decodeQuotedArgument(raw);
        if (targetType == byte.class || targetType == Byte.class) {
            try { return new java.math.BigDecimal(str).byteValueExact(); }
            catch (Exception e) { throw new IllegalArgumentException("Expected an in-range integer for byte: " + str, e); }
        } else if (targetType == short.class || targetType == Short.class) {
            try { return new java.math.BigDecimal(str).shortValueExact(); }
            catch (Exception e) { throw new IllegalArgumentException("Expected an in-range integer for short: " + str, e); }
        } else if (targetType == int.class || targetType == Integer.class) {
            if (str.startsWith("#")) {
                try { return Color.parseColor(str); }
                catch (Exception e) { throw new IllegalArgumentException("Invalid color literal: " + str, e); }
            }
            try { return new java.math.BigDecimal(str).intValueExact(); }
            catch (Exception e) { throw new IllegalArgumentException("Expected an integer for " + targetType.getSimpleName() + ": " + str, e); }
        } else if (targetType == float.class || targetType == Float.class) {
            try {
                float value = Float.parseFloat(str);
                if (Float.isInfinite(value) || Float.isNaN(value)) throw new NumberFormatException("Non-finite value");
                return value;
            } catch (Exception e) { throw new IllegalArgumentException("Expected a finite number for float: " + str, e); }
        } else if (targetType == double.class || targetType == Double.class) {
            try {
                double value = Double.parseDouble(str);
                if (Double.isInfinite(value) || Double.isNaN(value)) throw new NumberFormatException("Non-finite value");
                return value;
            } catch (Exception e) { throw new IllegalArgumentException("Expected a finite number for double: " + str, e); }
        } else if (targetType == long.class || targetType == Long.class) {
            try { return new java.math.BigDecimal(str).longValueExact(); }
            catch (Exception e) { throw new IllegalArgumentException("Expected an integer for long: " + str, e); }
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            if ("true".equalsIgnoreCase(str)) return Boolean.TRUE;
            if ("false".equalsIgnoreCase(str)) return Boolean.FALSE;
            throw new IllegalArgumentException("Expected true or false for boolean: " + str);
        } else if (targetType == String.class) {
            return str;
        }
        throw new IllegalArgumentException("Unsupported parameter type: " + targetType.getName());
    }

    private Object getDefaultValue(Class<?> type) {
        if (type == int.class || type == Integer.class) return 0;
        if (type == float.class || type == Float.class) return 0.0f;
        if (type == double.class || type == Double.class) return 0.0;
        if (type == long.class || type == Long.class) return 0L;
        if (type == boolean.class || type == Boolean.class) return false;
        if (type == String.class) return "";
        return null;
    }

    private void setupTargetViewsList() {
        setupTargetViewsList(false);
    }

    private void setupTargetViewsList(boolean requireManualSelection) {
        if (targetViewSpinner == null) return;
        availableTargetViews.clear();
        availableTargetNames.clear();
        targetViewSnapshots.clear();
        if (requireManualSelection) {
            availableTargetViews.add(null);
            availableTargetNames.add("Select a target after rescan");
        }

        View mainView = defaultTargetComponent != null ? defaultTargetComponent.getView() : null;
        if (mainView != null) {
            addAvailableTargetView(mainView, "Target Layout (" + mainView.getClass().getSimpleName() + ")");
            if (mainView instanceof ViewGroup) {
                findChildViews((ViewGroup) mainView, "Child");
            }
        }

        ViewGroup rootDecor = (ViewGroup) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        if (rootDecor != null && rootDecor != mainView) {
            addAvailableTargetView(rootDecor, "Root Screen (FrameLayout)");
            // Root Screen च्या सर्व descendants पण जोडा — जेणेकरून Kodular मध्ये
            // नवीन add केलेले Label, Layout, Button इत्यादी आपोआप dropdown मध्ये दिसतील.
            findChildViews(rootDecor, "Root");
        }

        // BUG 2 FIX: दोन-ओळी custom adapter — मुख्य label friendly view name (बोल्ड),
        // secondary लहान राखाडी path. Truncation + scrolling योग्य.
        ArrayAdapter<String> targetAdapter = new ArrayAdapter<String>(
                activity, android.R.layout.simple_spinner_item, availableTargetNames) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                return buildTargetLabel(position, convertView, parent, false);
            }
            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                return buildTargetLabel(position, convertView, parent, true);
            }
            private View buildTargetLabel(int position, View convertView, android.view.ViewGroup parent, boolean dropdown) {
                String raw = (position >= 0 && position < availableTargetNames.size())
                        ? availableTargetNames.get(position) : "";
                // "FriendlyName  ·  path" मधून friendly आणि path वेगळे कर.
                String friendly = raw;
                String path = "";
                int sep = raw.indexOf("  ·  ");
                if (sep >= 0) {
                    friendly = raw.substring(0, sep);
                    path = raw.substring(sep + 5);
                }
                LinearLayout row = new LinearLayout(activity);
                row.setOrientation(LinearLayout.VERTICAL);
                int hPad = dipToPx(dropdown ? 14 : 10);
                int vPad = dipToPx(dropdown ? 10 : 6);
                row.setPadding(hPad, vPad, hPad, vPad);

                TextView primary = new TextView(activity);
                primary.setText(friendly);
                primary.setTextSize(dropdown ? 15f : 14f);
                primary.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
                primary.setTextColor(Color.parseColor("#1E293B"));
                primary.setSingleLine(true);
                primary.setEllipsize(android.text.TextUtils.TruncateAt.END);
                row.addView(primary, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                if (!path.isEmpty()) {
                    TextView secondary = new TextView(activity);
                    secondary.setText(path);
                    secondary.setTextSize(11f);
                    secondary.setTextColor(Color.parseColor("#94A3B8"));
                    secondary.setSingleLine(true);
                    secondary.setEllipsize(android.text.TextUtils.TruncateAt.START);
                    row.addView(secondary, new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
                return row;
            }
        };
        targetViewSpinner.setAdapter(targetAdapter);
        targetViewSpinner.setSelection(0);
    }

    private void findChildViews(ViewGroup group, String prefix) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (isBoltPanelView(child)) continue;
            // BUG 2 FIX: मुख्य label मध्ये friendly, वाचनीय view name;
            // लांबलचक hierarchy path (Root.1.1.1) secondary म्हणून.
            String friendly = getFriendlyViewName(child);
            String path = prefix + " › " + (i + 1);
            String name = friendly + "  ·  " + path;
            addAvailableTargetView(child, name);
            if (child instanceof ViewGroup) {
                findChildViews((ViewGroup) child, path);
            }
        }
    }

    // BUG 2 FIX: Kodular/App Inventor सारखी स्वच्छ, वाचनीय view नावे.
    private String getFriendlyViewName(View view) {
        if (view == null) return "View";
        String cls = view.getClass().getName();
        // App Inventor / Kodular component wrappers
        if (cls.contains("Label")) return "Label";
        if (cls.contains("Button")) return "Button";
        if (cls.contains("TextBox") || cls.contains("EditText")) return "TextBox";
        if (cls.contains("ImageView") || cls.contains("Image")) return "Image";
        if (cls.contains("CheckBox")) return "CheckBox";
        if (cls.contains("Switch")) return "Switch";
        if (cls.contains("Slider")) return "Slider";
        if (cls.contains("Spinner") || cls.contains("ListPicker")) return "ListPicker";
        if (cls.contains("ListView") || cls.contains("RecyclerView")) return "ListView";
        if (cls.contains("WebView")) return "WebView";
        if (cls.contains("HorizontalArrangement") || cls.contains("HorizontalScrollView")) return "HorizontalArrangement";
        if (cls.contains("VerticalArrangement")) return "VerticalArrangement";
        if (cls.contains("TableArrangement")) return "TableArrangement";
        if (cls.contains("ScrollView") || cls.contains("NestedScrollView")) return "ScrollView";
        if (cls.contains("CardView")) return "CardView";
        // Raw Android view fallbacks — simple name
        String simple = view.getClass().getSimpleName();
        if (simple.equals("LinearLayout")) return "LinearLayout";
        if (simple.equals("FrameLayout")) return "FrameLayout";
        if (simple.equals("RelativeLayout")) return "RelativeLayout";
        if (simple.equals("ConstraintLayout")) return "ConstraintLayout";
        if (simple.equals("TableLayout")) return "TableLayout";
        if (simple.equals("GridLayout")) return "GridLayout";
        return simple.isEmpty() ? "View" : simple;
    }

    private void postToMain(Runnable runnable) {
        mainHandler.post(runnable);
    }

    private void populateAutoPanel() {
        if (methodSpinner == null || currentExtensionClass == null) return;

        Method previousSelection = getSelectedMethodEntry();
        String previousIdentity = previousSelection != null ? formatMethodIdentity(previousSelection) : "";
        final List<String> methodsList = new ArrayList<String>();
        currentLoadedMethodEntries.clear();

        for (Method m : currentExtensionClass.getMethods()) {
            if (m.getDeclaringClass() == currentExtensionClass && !m.isBridge() && !m.isSynthetic()) {
                methodsList.add(formatMethodIdentity(m));
                currentLoadedMethodEntries.add(m);
            }
        }

        if (methodsList.isEmpty()) return;

        methodAdapter = new KodularBlockAdapter(activity, methodsList);
        methodSpinner.setAdapter(methodAdapter);

        methodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < currentLoadedMethodEntries.size()) {
                    Method selectedMethod = currentLoadedMethodEntries.get(position);
                    userSelectedMethod = selectedMethod.getName();
                    if (argsEditText != null) argsEditText.clearFocus();

                    autoSuggestBestTargetView(selectedMethod);

                    updateParamInfo(userSelectedMethod);
                    updateBuiltInStatusCard("Run: " + formatCompactMethodLabel(selectedMethod), null, null, Color.parseColor("#0F172A"));
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        int restoreIdx = -1;
        for (int i = 0; i < currentLoadedMethodEntries.size(); i++) {
            Method candidate = currentLoadedMethodEntries.get(i);
            if (!previousIdentity.isEmpty() && formatMethodIdentity(candidate).equals(previousIdentity)) {
                restoreIdx = i;
                break;
            }
            if (candidate.getName().equals(userSelectedMethod)) {
                restoreIdx = i;
                break;
            }
        }
        if (restoreIdx >= 0) {
            methodSpinner.setSelection(restoreIdx);
        } else {
            userSelectedMethod = currentLoadedMethodEntries.get(0).getName();
            methodSpinner.setSelection(0);
        }
        updateParamInfo(userSelectedMethod);
        if (restoreIdx >= 0) {
            Method restoredMethod = currentLoadedMethodEntries.get(restoreIdx);
            autoRenderPendingMethodIdentity = formatMethodIdentity(restoredMethod);
            initializeRestoredMethodViewIfVerified(restoredMethod);
        } else {
            autoRenderPendingMethodIdentity = "";
        }
    }

    private void updateParamInfo(String methodName) {
        if (currentExtensionClass == null || paramInfoLabel == null || argsEditText == null) {
            refreshProviderSelectionUi(null);
            return;
        }

        Method targetMethod = getSelectedMethodEntry();
        if (targetMethod == null || !targetMethod.getName().equals(methodName)) {
            targetMethod = findMethodByName(methodName);
        }
        if (targetMethod != null) {
            Class<?>[] types = targetMethod.getParameterTypes();

            int offset = (types.length > 0 && (AndroidViewComponent.class.isAssignableFrom(types[0]) || View.class.isAssignableFrom(types[0]))) ? 1 : 0;
            int reqUserParams = types.length - offset;

            if (reqUserParams == 0) {
                if (types.length > 0) {
                    paramInfoLabel.setText(tx("guide.parametersLabel") + " [" + tx("guide.autoTarget") + "]");
                } else {
                    paramInfoLabel.setText(tx("guide.parametersNone"));
                }
                argsEditText.setHint(tx("parameter.noArgs"));
                argsEditText.setText("");
                refreshProviderSelectionUi(targetMethod);
                return;
            }

            StringBuilder infoSb = new StringBuilder(tx("guide.parametersLabel") + " ");
            for (int i = offset; i < types.length; i++) {
                if (i > offset) infoSb.append(", ");
                infoSb.append(formatParameterLabel(targetMethod, i));
            }
            paramInfoLabel.setText(infoSb.toString());

            String cacheKey = getMethodArgumentsCacheKey(targetMethod);
            SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            String savedArgs = prefs.getString(cacheKey,
                    prefs.getString((currentClassName != null ? currentClassName : "Bolt") + ":" + methodName, ""));

            String smartDefault = generateTypedBlockDefaults(targetMethod);
            boolean savedArgsMatch = false;
            try {
                savedArgsMatch = !savedArgs.isEmpty() && parseArgumentTokens(savedArgs).size() == reqUserParams;
            } catch (IllegalArgumentException ignored) {}

            if (savedArgsMatch) {
                argsEditText.setText(savedArgs);
            } else {
                // Build smart default, replacing parameter positions with selected getter names
                try {
                    List<String> defaultTokens = parseArgumentTokens(smartDefault);
                    StringBuilder merged = new StringBuilder();
                    for (int k = 0; k < defaultTokens.size(); k++) {
                        String selectionKey = getPropertySelectionKey(targetMethod, k);
                        String getterName = selectedGetterProperties.get(selectionKey);
                        if (getterName != null && !getterName.isEmpty()) {
                            merged.append(getterName);
                        } else {
                            merged.append(defaultTokens.get(k));
                        }
                        if (k < defaultTokens.size() - 1) merged.append(", ");
                    }
                    argsEditText.setText(merged.toString());
                } catch (Exception e) {
                    argsEditText.setText(smartDefault);
                }
            }
            refreshProviderSelectionUi(targetMethod);
        }
    }

    private void showFlexibleTuningDialog() {
        if (activity == null || currentExtensionClass == null) return;
        final String methodName = userSelectedMethod;
        if (methodName == null || methodName.isEmpty()) return;

        Method targetMethod = getSelectedMethodEntry();
        if (targetMethod == null) return;

        final Method finalMethod = targetMethod;
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(activity);
        final ScrollView scroll = new ScrollView(activity);
        final LinearLayout dialogContent = new LinearLayout(activity);
        dialogContent.setOrientation(LinearLayout.VERTICAL);
        dialogContent.setPadding(dipToPx(16), dipToPx(14), dipToPx(16), dipToPx(14));

        TextView heading = new TextView(activity);
        BlockPropertyMetadata setterProperty = findVerifiedPropertySetter(finalMethod);
        heading.setText(tx("tune.title", setterProperty != null ? setterProperty.name : finalMethod.getName()));
        heading.setTextSize(15f);
        heading.setTypeface(null, Typeface.BOLD);
        heading.setTextColor(Color.parseColor("#0F172A"));
        heading.setPadding(0, 0, 0, dipToPx(10));
        dialogContent.addView(heading);

        // १. टार्गेट साईझ व लेआउट कंट्रोल्स
        TextView szLabel = new TextView(activity);
        szLabel.setText(tx("tune.sizeLayout"));
        szLabel.setTextSize(12f);
        szLabel.setTypeface(null, Typeface.BOLD);
        szLabel.setTextColor(Color.parseColor("#334155"));
        dialogContent.addView(szLabel);

        final View target = getSelectedTargetNativeView();
        final TextView hText = new TextView(activity);
        final TextView wText = new TextView(activity);
        int curH = 260, curW = 300;
        if (target != null && target.getLayoutParams() != null) {
            if (target.getLayoutParams().height > 0) curH = safePxToDip(target.getLayoutParams().height);
            if (target.getLayoutParams().width > 0) curW = safePxToDip(target.getLayoutParams().width);
        }
        hText.setText(tx("tune.height", curH));
        wText.setText(tx("tune.width", curW));
        hText.setTextColor(Color.parseColor("#475569"));
        wText.setTextColor(Color.parseColor("#475569"));

        dialogContent.addView(hText);
        SeekBar sbH = new SeekBar(activity);
        sbH.setMax(650);
        sbH.setProgress(curH);
        sbH.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean b) {
                if (p < 40) p = 40;
                hText.setText(tx("tune.height", p));
                updateTargetNativeSize(p, -1);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
        dialogContent.addView(sbH);

        dialogContent.addView(wText);
        SeekBar sbW = new SeekBar(activity);
        sbW.setMax(650);
        sbW.setProgress(curW);
        sbW.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean b) {
                if (p < 40) p = 40;
                wText.setText(tx("tune.width", p));
                updateTargetNativeSize(-1, p);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
        dialogContent.addView(sbW);

        View div = new View(activity);
        div.setBackgroundColor(Color.parseColor("#CBD5E1"));
        LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dipToPx(1));
        divLp.setMargins(0, dipToPx(8), 0, dipToPx(8));
        dialogContent.addView(div, divLp);

        // २. पॅरामीटर्स इनपुट विभाग
        Class<?>[] pTypes = finalMethod.getParameterTypes();
        String rawArgs = (argsEditText != null) ? argsEditText.getText().toString().trim() : "";
        List<String> curArgs = new ArrayList<String>();
        try {
            curArgs = parseArgumentTokens(rawArgs);
        } catch (IllegalArgumentException e) {
            logToConsole("[INPUT-ERR] " + e.getMessage());
        }
        final List<String> detectedParamNames = extractParamNames(finalMethod);

        int offset = 0;
        if (pTypes.length > 0 && (AndroidViewComponent.class.isAssignableFrom(pTypes[0]) || View.class.isAssignableFrom(pTypes[0]))) {
            offset = 1;
        }

        final int totalUserParams = pTypes.length - offset;
        final String[] currentParamValues = new String[totalUserParams];
        final EditText[] activeInputs = new EditText[totalUserParams];
        final String[] getterSelections = new String[totalUserParams];

        for (int k = 0; k < totalUserParams; k++) {
            String selectionKey = getPropertySelectionKey(finalMethod, k);
            getterSelections[k] = selectedGetterProperties.get(selectionKey);
            if (k < curArgs.size() && !curArgs.get(k).trim().isEmpty()) {
                currentParamValues[k] = decodeQuotedArgument(curArgs.get(k));
            } else {
                String pName = (k + offset < detectedParamNames.size()) ? detectedParamNames.get(k + offset) : ("param" + (k + 1));
                Class<?> parameterType = pTypes[k + offset];
                BlockArgumentMetadata metadata = getBlockArgumentMetadata(finalMethod, k + offset);
                if (YailList.class.isAssignableFrom(parameterType)) {
                    String verifiedOrEmptyList = getTypedBlockDefault(parameterType, metadata);
                    currentParamValues[k] = verifiedOrEmptyList != null ? verifiedOrEmptyList : "[]";
                } else {
                    currentParamValues[k] = getSmartDefaultValue(pName, parameterType, k + 1, finalMethod.getName());
                }
            }
        }

        for (int i = offset; i < pTypes.length; i++) {
            final int argIdx = i - offset;
            final Class<?> pType = pTypes[i];
            final String argVal = currentParamValues[argIdx];

            String fieldName = (i < detectedParamNames.size()) ? detectedParamNames.get(i) : ("param" + (argIdx + 1));
            fieldName = fieldName.replace("(Auto)", "").trim();

            final boolean isColor = isColorParameter(fieldName, finalMethod.getName(), pType, argVal);
            final BlockArgumentMetadata argumentMetadata = getBlockArgumentMetadata(finalMethod, i);
            final boolean isVerifiedColorList = YailList.class.isAssignableFrom(pType)
                    && getVerifiedColorListElementFormat(argumentMetadata) != 0;

            TextView lbl = new TextView(activity);
                lbl.setText(formatParameterLabel(finalMethod, i) + ":");
            lbl.setTextSize(11.5f);
            lbl.setTypeface(null, Typeface.BOLD);
            lbl.setTextColor(Color.parseColor("#1E293B"));
            lbl.setPadding(0, dipToPx(6), 0, dipToPx(3));
            dialogContent.addView(lbl);

            List<BlockPropertyMetadata> getterProperties = getCompatibleGetterProperties(pType);

            // ===== सुधारणा २: Getters alphabetically sort =====
            java.util.Collections.sort(getterProperties, new java.util.Comparator<BlockPropertyMetadata>() {
                @Override
                public int compare(BlockPropertyMetadata a, BlockPropertyMetadata b) {
                    return a.name.compareToIgnoreCase(b.name);
                }
            });

            final List<String> getterOptions = new ArrayList<String>();
            if (!getterProperties.isEmpty()) {
                getterOptions.add("Choose Getter (optional)");
                for (BlockPropertyMetadata property : getterProperties) {
                    getterOptions.add(property.name + " (" + property.type + ")");
                }
            }
            // Compatible getters नसतील तर dropdown अजिबात दाखवू नका.
            if (!getterOptions.isEmpty()) {
                final Spinner getterSpinner = new Spinner(activity);
                ArrayAdapter<String> getterAdapter = new ArrayAdapter<String>(
                        activity, android.R.layout.simple_spinner_item, getterOptions);
                getterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                getterSpinner.setAdapter(getterAdapter);
                int getterSelection = 0;
                for (int option = 0; option < getterProperties.size(); option++) {
                    if (getterProperties.get(option).name.equals(getterSelections[argIdx])) getterSelection = option + 1;
                }
                if (getterSelection == 0) getterSelections[argIdx] = null;
                getterSpinner.setSelection(getterSelection);
                dialogContent.addView(getterSpinner, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, dipToPx(40)));
                getterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        getterSelections[argIdx] = position > 0 && position <= getterProperties.size()
                                ? getterProperties.get(position - 1).name : null;
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) {}
                });
            }

            // Boolean literal dropdown
            if (pType == boolean.class || pType == Boolean.class || fieldName.toLowerCase(Locale.ROOT).contains("animate") || fieldName.toLowerCase(Locale.ROOT).contains("enable") || fieldName.toLowerCase(Locale.ROOT).contains("flat") || fieldName.toLowerCase(Locale.ROOT).contains("visible")) {
                final Spinner boolSpinner = new Spinner(activity);
                final String[] boolOptions = {"true", "false"};
                ArrayAdapter<String> badp = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_dropdown_item, boolOptions);
                boolSpinner.setAdapter(badp);
                boolSpinner.setSelection("false".equalsIgnoreCase(argVal) ? 1 : 0);

                GradientDrawable bBg = new GradientDrawable();
                bBg.setColor(Color.parseColor("#F1F5F9"));
                bBg.setCornerRadius(dipToPx(6));
                bBg.setStroke(dipToPx(1), Color.parseColor("#CBD5E1"));
                boolSpinner.setBackground(bBg);

                LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dipToPx(40));
                bLp.setMargins(0, dipToPx(2), 0, dipToPx(6));
                boolSpinner.setLayoutParams(bLp);

                boolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                        String chosen = boolOptions[pos];
                        currentParamValues[argIdx] = chosen;
                        updateArgAtPos(argIdx, chosen);
                    }
                    @Override public void onNothingSelected(AdapterView<?> p) {}
                });
                dialogContent.addView(boolSpinner);
            }
            // C. Text, Numbers & Colors
            else {
                final EditText ed = new EditText(activity);
                ed.setText(argVal);
                ed.setTextSize(12f);
                ed.setTextColor(Color.parseColor("#0F172A"));
                GradientDrawable edBg = new GradientDrawable();
                edBg.setColor(Color.WHITE);
                edBg.setCornerRadius(dipToPx(6));
                edBg.setStroke(dipToPx(1), Color.parseColor("#CBD5E1"));
                ed.setBackground(edBg);
                ed.setPadding(dipToPx(8), dipToPx(6), dipToPx(8), dipToPx(6));
                activeInputs[argIdx] = ed;

                ed.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String clean = s.toString().trim();
                        currentParamValues[argIdx] = clean;
                        updateArgAtPos(argIdx, clean);
                    }
                    @Override public void afterTextChanged(Editable s) {}
                });
                if (isVerifiedColorList) {
                    ed.setVisibility(View.GONE);
                    renderMainColorListEditor(dialogContent, ed, currentParamValues, argIdx,
                            argumentMetadata);
                } else {
                    dialogContent.addView(ed);
                    addUnverifiedColorListMetadataNotice(dialogContent, pType, argumentMetadata);
                }

                if (isColor && !YailList.class.isAssignableFrom(pType)) {
                    HorizontalScrollView hsv = new HorizontalScrollView(activity);
                    hsv.setPadding(0, dipToPx(3), 0, dipToPx(6));
                    LinearLayout palRow = new LinearLayout(activity);
                    palRow.setOrientation(LinearLayout.HORIZONTAL);

                    for (final String cHex : EXTENDED_COLOR_PALETTE) {
                        Button cBtn = new Button(activity);
                        cBtn.setText(cHex);
                        cBtn.setTextSize(7f);
                        cBtn.setTextColor(cHex.equals("#FFFFFF") || cHex.equals("#EAB308") || cHex.equals("#E0E5EC") ? Color.BLACK : Color.WHITE);
                        GradientDrawable cBg = new GradientDrawable();
                        cBg.setColor(Color.parseColor(cHex));
                        cBg.setCornerRadius(dipToPx(5));
                        cBtn.setBackground(cBg);

                        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(dipToPx(64), dipToPx(30));
                        cLp.setMargins(dipToPx(2), 0, dipToPx(2), 0);
                        cBtn.setLayoutParams(cLp);

                        cBtn.setOnClickListener(new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                ed.setText(cHex);
                                currentParamValues[argIdx] = cHex;
                                updateArgAtPos(argIdx, cHex);
                            }
                        });
                        palRow.addView(cBtn);
                    }
                    hsv.addView(palRow);
                    dialogContent.addView(hsv);
                }

                boolean isPureNumeric = (pType == int.class || pType == Integer.class || pType == float.class || pType == Float.class || pType == double.class) && !isColor;
                if (isPureNumeric) {
                    int numVal = 10;
                    try { numVal = (int) Double.parseDouble(argVal); } catch (Exception ignored) {}
                    SeekBar sb = new SeekBar(activity);
                    sb.setMax(Math.max(100, numVal * 3));
                    sb.setProgress(numVal);
                    sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                            if (fromUser) {
                                ed.setText(String.valueOf(progress));
                                currentParamValues[argIdx] = String.valueOf(progress);
                                updateArgAtPos(argIdx, String.valueOf(progress));
                            }
                        }
                        @Override public void onStartTrackingTouch(SeekBar s) {}
                        @Override public void onStopTrackingTouch(SeekBar s) {}
                    });
                    dialogContent.addView(sb);
                }
            }
        }

        refreshProviderSelectionUi(finalMethod);
        if (!providerSelectionSlots.isEmpty()) {
            TextView providerHeading = new TextView(activity);
            providerHeading.setText("Optional value providers (selected provider runs before this method)");
            providerHeading.setTextSize(12f);
            providerHeading.setTypeface(null, Typeface.BOLD);
            providerHeading.setTextColor(Color.parseColor("#334155"));
            providerHeading.setPadding(0, dipToPx(12), 0, dipToPx(4));
            dialogContent.addView(providerHeading);

            LinearLayout providerDialogContainer = new LinearLayout(activity);
            providerDialogContainer.setOrientation(LinearLayout.VERTICAL);
            providerDialogContainer.setPadding(dipToPx(8), dipToPx(6), dipToPx(8), dipToPx(6));
            GradientDrawable providerDialogBackground = new GradientDrawable();
            providerDialogBackground.setColor(Color.WHITE);
            providerDialogBackground.setCornerRadius(dipToPx(8));
            providerDialogBackground.setStroke(dipToPx(1), Color.parseColor("#DDD6FE"));
            providerDialogContainer.setBackground(providerDialogBackground);
            renderSelectedProviderViews(finalMethod, providerDialogContainer);
            dialogContent.addView(providerDialogContainer);
        }

        scroll.addView(dialogContent);
        builder.setView(scroll);

        builder.setPositiveButton(tx("button.done"), new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
                applyAndExecuteFromDialog(currentParamValues, activeInputs, getterSelections, finalMethod);
            }
        });

        tuningDialog = builder.create();
        tuningDialog.show();
        if (!selectedProviderConfigurations.isEmpty()) {
            scheduleProviderPreviewIfReady(finalMethod);
        }
    }

    private void applyAndExecuteFromDialog(String[] values, EditText[] inputs,
                                           String[] getterSelections, Method selectedMethod) {
        if (argsEditText == null) return;

        for (int i = 0; i < values.length; i++) {
            String selectionKey = getPropertySelectionKey(selectedMethod, i);
            if (getterSelections != null && i < getterSelections.length && getterSelections[i] != null) {
                selectedGetterProperties.put(selectionKey, getterSelections[i]);
            } else {
                selectedGetterProperties.remove(selectionKey);
            }
            if (inputs != null && i < inputs.length && inputs[i] != null) {
                values[i] = inputs[i].getText().toString().trim();
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < values.length; j++) {
            String selectionKeyForDisplay = getPropertySelectionKey(selectedMethod, j);
            String getterNameForDisplay = selectedGetterProperties.get(selectionKeyForDisplay);
            if (getterNameForDisplay != null && !getterNameForDisplay.isEmpty()) {
                sb.append(getterNameForDisplay);
            } else {
                sb.append(formatArgumentToken(values[j] != null ? values[j].trim() : ""));
            }
            if (j < values.length - 1) sb.append(", ");
        }
        final String finalArgs = sb.toString();

        activity.runOnUiThread(new Runnable() {
            @Override public void run() {
                argsEditText.setText(finalArgs);
            }
        });

        String cacheKey = getMethodArgumentsCacheKey(selectedMethod);
        try {
            SharedPreferences prefs = activity.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            prefs.edit().putString(cacheKey, finalArgs).apply();
            argumentsCache.put(cacheKey, finalArgs);
        } catch (Exception ignored) {}

        logToConsole("[TUNED] Applied: " + selectedMethod.getName() + "(" + finalArgs + ")");
        executeSelectedMethodWithProvidersFromUI();
    }

    // ==========================================
    // ४. अस्सल कोडलर ब्लॉक ॲडॉप्टर
    // ==========================================
    private class KodularBlockAdapter extends ArrayAdapter<String> {
        public KodularBlockAdapter(Context context, List<String> methods) {
            super(context, 0, methods);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return buildAuthenticBlock(position, convertView, parent, false);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return buildAuthenticBlock(position, convertView, parent, true);
        }

        private View buildAuthenticBlock(int position, View convertView, ViewGroup parent, boolean isDropdown) {
            String methodLabel = getItem(position);
            Context ctx = getContext();

            // Outer container — spacing साठी
            LinearLayout outer = new LinearLayout(ctx);
            outer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams outerLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            outerLp.setMargins(0, dipToPx(3), 0, dipToPx(3));
            outer.setLayoutParams(outerLp);

            Method m = position >= 0 && position < currentLoadedMethodEntries.size()
                    ? currentLoadedMethodEntries.get(position) : null;
            BlockPropertyMetadata setterProperty = m != null ? findVerifiedPropertySetter(m) : null;
            String methodName = setterProperty != null ? setterProperty.name : (m != null ? m.getName() : methodLabel);

            // Block रंग आणि prefix — खरे Kodular ब्लॉक्स सारखे
            int blockColor;
            String blockPrefix;
            if (m != null && m.getReturnType() != void.class && m.getParameterTypes().length == 0) {
                // Getter (value return, no args) — Kodular convention: हिरवा "get"
                blockColor = Color.parseColor("#4CAF50");
                blockPrefix = "get";
            } else if (m != null && isVerifiedPropertySetter(m)) {
                // Verified SimpleProperty setter — हिरवा "set"
                blockColor = Color.parseColor("#4CAF50");
                blockPrefix = "set";
            } else if (m != null && m.getReturnType() == void.class && m.getParameterTypes().length > 0) {
                // SimpleFunction — जांभळा "call"
                blockColor = Color.parseColor("#6C3EB8");
                blockPrefix = "call";
            } else {
                // Default — जांभळा "call"
                blockColor = Color.parseColor("#6C3EB8");
                blockPrefix = "call";
            }

            // खरे Kodular block — आत inner container
            LinearLayout block = new LinearLayout(ctx);
            block.setOrientation(LinearLayout.VERTICAL);
            block.setPadding(dipToPx(10), dipToPx(7), dipToPx(10), dipToPx(7));

            GradientDrawable blockBg = new GradientDrawable();
            blockBg.setColor(blockColor);
            blockBg.setCornerRadius(dipToPx(6));
            block.setBackground(blockBg);

            // --- मुख्य ओळ: prefix + [Component] + .MethodName ---
            LinearLayout mainRow = new LinearLayout(ctx);
            mainRow.setOrientation(LinearLayout.HORIZONTAL);
            mainRow.setGravity(Gravity.CENTER_VERTICAL);

            // Prefix (call/set/when)
            TextView prefixView = new TextView(ctx);
            prefixView.setText(blockPrefix + " ");
            prefixView.setTextSize(10f);
            prefixView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            prefixView.setTextColor(Color.WHITE);
            mainRow.addView(prefixView);

            // [Component] badge
            String compName = (currentExtensionClass != null)
                    ? currentExtensionClass.getSimpleName() : "Component";
            TextView compBadge = new TextView(ctx);
            compBadge.setText(" " + compName + " ");
            compBadge.setTextSize(10f);
            compBadge.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            compBadge.setTextColor(Color.parseColor("#1E1E1E"));
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setColor(Color.parseColor("#E8DEF8"));
            badgeBg.setCornerRadius(dipToPx(3));
            compBadge.setBackground(badgeBg);
            compBadge.setPadding(dipToPx(6), dipToPx(2), dipToPx(6), dipToPx(2));
            mainRow.addView(compBadge);

            // .MethodName
            TextView methodView = new TextView(ctx);
            methodView.setText(" ." + methodName);
            methodView.setTextSize(11f);
            methodView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            methodView.setTextColor(Color.WHITE);
            methodView.setSingleLine(true);
            methodView.setEllipsize(android.text.TextUtils.TruncateAt.END);
            mainRow.addView(methodView);

            block.addView(mainRow);

            // --- Dropdown मध्येच parameters एका ओळीत ---
            if (isDropdown && m != null) {
                Class<?>[] pTypes = m.getParameterTypes();
                if (pTypes.length > 0) {
                    StringBuilder paramLine = new StringBuilder();
                    for (int i = 0; i < pTypes.length; i++) {
                        if (i > 0) paramLine.append(", ");
                        paramLine.append(pTypes[i].getSimpleName());
                    }
                    TextView paramView = new TextView(ctx);
                    paramView.setText("(" + pTypes.length + ") " + paramLine);
                    paramView.setTextSize(9f);
                    paramView.setTypeface(Typeface.MONOSPACE);
                    paramView.setTextColor(Color.parseColor("#E0E7FF"));
                    paramView.setSingleLine(true);
                    paramView.setEllipsize(android.text.TextUtils.TruncateAt.END);
                    paramView.setPadding(dipToPx(18), dipToPx(2), 0, 0);
                    block.addView(paramView);
                }
            }

            outer.addView(block);
            return outer;
        }
    }

    // ===== Events =====
    @SimpleEvent(description = "Triggered when connected PC IP is discovered")
    public void ClientIPFound(String ip) { EventDispatcher.dispatchEvent(this, "ClientIPFound", ip); }

    @SimpleEvent(description = "Triggered when UDP status changes or ping sent")
    public void DiscoverySent(String message, int port) { EventDispatcher.dispatchEvent(this, "DiscoverySent", message, port); }

    @SimpleEvent(description = "Triggered when WebSocket connected")
    public void Connected() { EventDispatcher.dispatchEvent(this, "Connected"); }

    @SimpleEvent(description = "Triggered when WebSocket disconnected")
    public void Disconnected(int code, String reason) { EventDispatcher.dispatchEvent(this, "Disconnected", code, reason); }

    @SimpleEvent(description = "Triggered when a message is received")
    public void MessageReceived(String message) { EventDispatcher.dispatchEvent(this, "MessageReceived", message); }

    @SimpleEvent(description = "Triggered when binary data (DEX) is received")
    public void BinaryReceived(int data) { EventDispatcher.dispatchEvent(this, "BinaryReceived", data); }

    @SimpleEvent(description = "Triggered when classes.dex is saved")
    public void DexSaved(String path, long size) { EventDispatcher.dispatchEvent(this, "DexSaved", path, size); }

    @SimpleEvent(description = "Triggered when DEX is invalid")
    public void DexInvalid(String message) {
        lastDexVerificationStatus = "ERROR";
        lastDexVerificationMessage = message != null ? message : "DEX is invalid.";
        EventDispatcher.dispatchEvent(this, "DexInvalid", message);
    }

    @SimpleEvent(description = "Triggered when DEX header verification succeeds")
    public void DexVerified(String version, long size) {
        lastDexVerificationStatus = "PASS";
        lastDexVerificationMessage = "DEX magic header detected (version " + version + ", " + size + " bytes).";
        EventDispatcher.dispatchEvent(this, "DexVerified", version, size);
    }

    @SimpleEvent(description = "Triggered when DEX verification fails")
    public void DexVerificationFailed(String message) {
        lastDexVerificationStatus = "ERROR";
        lastDexVerificationMessage = message != null ? message : "DEX verification failed.";
        EventDispatcher.dispatchEvent(this, "DexVerificationFailed", message);
    }

    @SimpleEvent(description = "Triggered when any extension is auto-detected and loaded into memory")
    public void ExtensionReady(String className) { EventDispatcher.dispatchEvent(this, "ExtensionReady", className); }

    @SimpleEvent(description = "Triggered when a method runs successfully")
    public void MethodExecuted(String methodName) {
        lastExecutionStatus = "PASS";
        lastExecutionMethod = methodName != null ? methodName : "";
        lastExecutionMessage = "Method completed.";
        lastExecutionResult = "";
        if (!currentErrorMessage.isEmpty()) previousErrorMessage = currentErrorMessage;
        currentErrorMessage = "";
        EventDispatcher.dispatchEvent(this, "MethodExecuted", methodName);
    }

    @SimpleEvent(description = "Triggered when a method returns a value")
    public void MethodResultReceived(String methodName, String result) {
        lastExecutionStatus = "PASS";
        lastExecutionMethod = methodName != null ? methodName : "";
        lastExecutionMessage = "Getter returned a value.";
        lastExecutionResult = result != null ? result : "null";
        if (!currentErrorMessage.isEmpty()) previousErrorMessage = currentErrorMessage;
        currentErrorMessage = "";
        EventDispatcher.dispatchEvent(this, "MethodResultReceived", methodName, result);
    }

    @SimpleEvent(description = "Triggered when an error occurs")
    public void Error(String message) {
        String currentMessage = message != null ? message : "";
        if (!currentErrorMessage.isEmpty()) previousErrorMessage = currentErrorMessage;
        currentErrorMessage = currentMessage;
        lastErrorMessage = currentMessage;
        EventDispatcher.dispatchEvent(this, "Error", message);
    }
}
