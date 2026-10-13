# Repackages all the optimized classes into com.supertech.BoltWS.repackaged package in resulting
# AIX. Repackaging is necessary to avoid clashes with other extensions.
-repackageclasses com.supertech.BoltWS.repackaged

# Aggressive optimizations for smaller extension size
-android
-optimizationpasses 5
-allowaccessmodification
-mergeinterfacesaggressively
-overloadaggressively
-useuniqueclassmembernames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers