# Keep data classes for SharedPreferences serialization
-keep class com.vim.fasting.data.** { *; }

# Keep AlarmManager receiver
-keep class com.vim.fasting.receiver.** { *; }

# Keep notification channel setup
-keep class com.vim.fasting.notification.** { *; }
