# 🗓️ X-O Agenda · Aplicación Android en Kotlin

Aplicación Android desarrollada en **Kotlin** que permite gestionar recordatorios, eventos y alertas personales desde una interfaz moderna e intuitiva.  
El proyecto combina autenticación, calendario interactivo y notificaciones locales para crear una agenda funcional y visualmente atractiva.

---

## 🧩 Descripción general

**X-O Agenda** está pensada como una app de organización personal.  
Permite **crear, editar y eliminar eventos**, configurar **recordatorios con alertas**, y administrar el **perfil del usuario** con almacenamiento en la nube mediante **Firebase**.

Incluye:
- Pantalla de **inicio de sesión, registro y recuperación de cuenta**.  
- **Vista de calendario** interactiva con eventos marcados (usando `MaterialCalendarView`).  
- Sistema de **alertas** con notificaciones gestionadas desde `ReminderReceiver`.  
- Módulo de **perfil de usuario** editable (`ProfileFragment`, `EditProfileFragment`).  
- Integración con **Firebase Firestore** para almacenamiento de datos.  
- Preferencias configurables desde `SettingFragments`.

---

## 🚀 Características principales
- Arquitectura basada en **Activities + Fragments**.
- **Login y registro** con Firebase Authentication.  
- **Gestión de eventos** en Firestore (`FirestoreHelper.kt`).  
- **Notificaciones programadas** con `BroadcastReceiver`.  
- **Interfaz dinámica** con adaptadores (`Adapters`) para listas y calendario.  
- **Soporte multilenguaje** (carpetas `values` y `values-en`).  

---

## 🧠 Tecnologías utilizadas
- **Kotlin**
- **Android Studio**
- **Firebase (Firestore, Auth)**
- **MaterialCalendarView**
- **RecyclerView + Adapters**
- **SharedPreferences**
- **Fragments / Navigation**
- **BroadcastReceiver**
