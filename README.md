# 🧮 AI Math Solver & Calculator

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![AI](https://img.shields.io/badge/AI-Gemini%20API-orange.svg)](https://ai.google.dev)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

A smart, interactive **AI Calculator and Math Assistant** built with **Kotlin** and **Jetpack Compose**. It solves complex mathematical expressions, equations, and word problems step-by-step with high-precision **LaTeX formatting**, interactive formula explanations, and a local session history sidebar.

---

## ✨ Key Features

1. **Accurate Step-by-Step Solutions**:
   - Atomic intermediate steps showing the exact mathematical transformations.
   - Distinct titles, explanations, and guidance tips for every step.
   - Highlighted final result in bold LaTeX with one-tap clipboard copy.

2. **Multi-Domain Mathematical Engine**:
   - **📐 Algebra**: Quadratic equations, linear equations, systems of equations, polynomials, factoring.
   - **📈 Calculus**: Derivatives, definite & indefinite integrals, limits, series, and summation.
   - **🔢 Matrices**: Matrix addition, multiplication, $2 \times 2$ and $3 \times 3$ determinants, matrix inverses.
   - **📐 Trigonometry**: Sine, cosine, tangent, inverse trigonometric identities, radians & degrees.
   - **➕ Arithmetic**: PEMDAS/BODMAS precedence, fractions, powers, roots, scientific notation.
   - **💡 Word Problems**: Kinematics, geometry, percentage, rate, and financial word problems.

3. **🎓 "How?" Concept Explanations**:
   - Expandable **Formula & Concept Cheatsheet** explaining the underlying rules, theorems, and identities in simple terms.
   - Interactive **AI Math Tutor** modal sheet to ask follow-up questions (*"Explain like I'm 12"*, *"Why this method?"*, *"Show alternate method"*).

4. **⚠️ Mathematical Error Handling**:
   - Automatically detects invalid mathematical inputs (such as division by zero $\frac{a}{0}$ or undefined operations).
   - Explains clearly why division by zero cannot be evaluated in standard real arithmetic ($0 \cdot x \neq a$).

5. **🎨 Modern LaTeX Math Typography**:
   - Crisp rendering of fractions, roots, matrices, integrals, sums, and Greek symbols.
   - Full dark and light theme dynamic color schemes.

6. **💾 Local Session Sidebar (History)**:
   - Persistent history powered by an on-device **Room Database**.
   - Filter by domain (*Algebra*, *Calculus*, *Matrices*, *Trig*, *Word Problems*, *Starred*).
   - Real-time search and one-tap problem reloading.

---

## 📱 How to Run the App

### Option A: Install APK on Android Device
1. In Google AI Studio, open the top-right menu and select **Export > Download APK**.
2. Transfer the `.apk` file to your Android phone and install it directly.

### Option B: Open in Android Studio
1. Clone this repository:
   ```bash
   git clone https://github.com/your-username/ai-math-solver.git
   ```
2. Open the folder in **Android Studio (Ladybug or newer)**.
3. Let Gradle sync and press **Run (Shift + F10)** on an Android device or emulator.

---

## 🌐 GitHub Pages Live Demo

This repository includes a static showcase landing page for **GitHub Pages**.

### How to enable GitHub Pages:
1. Go to your repository on GitHub.
2. Click **Settings** ➡️ **Pages** (on the left sidebar).
3. Under **Build and deployment**:
   - **Source**: `Deploy from a branch`
   - **Branch**: `main` (or `master`)
   - **Folder**: `/ (root)` or `/docs`
4. Click **Save**. Your site will be live at `https://<username>.github.io/<repo-name>/`!

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.x
- **UI Toolkit**: Jetpack Compose (Material Design 3)
- **Local Persistence**: AndroidX Room Database & KSP
- **Networking**: Retrofit, OkHttp, Moshi
- **AI Backend**: Google Gemini API (`gemini-3.8-flash` / `gemini-3.5-flash`)
- **Math Engine**: KaTeX & Local Fallback Math Parser
