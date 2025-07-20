# **FridgeChef - Smart Recipe Recommendation App**  


## **Overview**  
**FridgeChef** is an **Android-based recipe recommendation app** that helps users discover recipes based on the ingredients they already have at home. Designed to minimize food waste and simplify meal planning, the app leverages **AI-powered ingredient recognition, nutritional analysis, and personalized recipe suggestions** to enhance the cooking experience.  

Built with **Java (Android Studio)** and integrated with **Firebase, Spoonacular API, and Large Language Models (LLMs)**, FridgeChef provides a seamless way to:  
✔ **Find recipes** based on available ingredients  
✔ **Track pantry items** with a digital inventory  
✔ **Analyze nutritional value** using AI  
✔ **Save and share recipes** effortlessly  

---

## **Key Features**  

### **1. Smart Recipe Recommendations**  
- **Recipe suggestions** based on pantry ingredients  
- **Missing ingredient alerts** for each recipe  
- **Personalized ranking** of recipes  

### **2. Digital Pantry Management**  
✔ **Manual entry** of ingredients  
✔ **AI-powered camera scanning** (using **OpenRouter API & GPT-4o-mini**)  
✔ **Auto-complete suggestions** (via **Spoonacular API**)  
✔ **Nutri-Score evaluation** to assess pantry healthiness  

### **3. Nutritional & AI Analysis**  
- **AI-generated nutritional breakdown** (via **Claude-3-Haiku**)  
- **Health score (1-10)** for each recipe  
- **Calorie & macronutrient estimation**  

### **4. Cooking Mode & Recipe Sharing**  
- **Step-by-step cooking guide** (slideshow mode)  
- **One-click sharing** (generates **HTML pages on GitHub**)  
- **Save favorites** to a personal **Cooking Book**  

### **5. User Statistics & Insights**  
**Top 10 most-used ingredients** across all users  
**Personal pantry health score** (Nutri-Score)  

---

## **Tech Stack**  

| **Category**       | **Technologies Used** |  
|--------------------|----------------------|  
| **Frontend**       | Java (Android Studio) |  
| **Backend**        | Firebase (Auth & Realtime DB) |  
| **APIs**           | Spoonacular API (Recipes), OpenRouter (LLMs) |  
| **AI Models**      | GPT-4o-mini (Image Recognition), Claude-3-Haiku (Nutrition Analysis) |  
| **Hosting**        | GitHub Pages (Recipe Sharing) |  

---

## **How It Works**  

### **1. Sign Up / Log In**  
- Secure authentication via **Firebase Auth**  

### **2. Set Up Your Pantry**  
**Option 1:** Scan ingredients with AI camera  
**Option 2:** Manually add ingredients with auto-suggest  

### **3. Recommended Recipes**  
- See **recommended recipes** based on your pantry  
- Filter by missing ingredients  

### **4. View Recipe Details**  
- Ingredients list  
- Step-by-step instructions  
- **AI-powered nutrition insights**  

### **5. Cook & Share**  
- Use **Cooking Mode** for guided instructions  
- **Save recipes** to your **Cooking Book**  
- **Share recipes** via **social media** (HTML link)  

---

## **Documentation & Thesis**  
For a **detailed technical breakdown**, including:  
✅ **System architecture**  
✅ **API integrations**  
✅ **AI implementation**  
✅ **User flow diagrams**  

**Read the full thesis:** [**Vasileiou_21010.pdf**](Vasileiou_21010.pdf)  

---

## **Future Improvements**  
🔹 **Multi-language support** (Greek & more)  
🔹 **Smart shopping list generator**  
🔹 **Recipe community & user posts**  
🔹 **Meal planning & grocery integration**  
🔹 **Enhanced AI recipe personalization**  

---

## **License**  
This project is **academic work** developed as part of an undergraduate thesis at the **University of Piraeus**.  
- **Educational use only**  
- **No commercial redistribution** without permission  

---

## **Developer**  
**Vasileios Panagiotis Vasileiou**  
📧 *Contact: [vasilisvasileiou33@gmail.com]*  
🔗 *GitHub: [https://github.com/VasilisVas1]*  

---

**FridgeChef** – **Cook smarter, waste less!** 🍳📱
