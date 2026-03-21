# Add project specific ProGuard rules here.

# Firebase
-keepattributes Signature
-keepattributes *Annotation*

# Hilt
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel
