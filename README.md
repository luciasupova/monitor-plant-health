# Monitor Plant Health

This project uses machine learning to classify plant leaves as healthy or diseased. The solution is built using TensorFlow Lite and deployed as a mobile app for real-time, on-device plant health monitoring. The model was trained on a subset of the PlantVillage dataset using Edge Impulse, and the classification system can be easily used by farmers through a simple mobile application. 

## Project Structure

This repository contains the following:
- **TensorFlow Lite model**: The trained model exported for mobile use.
- **Python Scripts**: Scripts used for dataset organization and preprocessing.
- **Mobile App**: The Android app developed to run the model on a smartphone.
- **Sample Dataset**: A small subset of images (about 20 images per class) used for model training.

## Overview

The goal of this project is to provide an easy-to-use tool for farmers to detect diseases in apple and corn leaves. The model was trained to classify leaves into two categories:
- **Healthy**
- **Diseased**

### Key Features
- **Real-time Disease Detection**: The app allows users to take pictures of plant leaves and classify them as healthy or diseased using the trained model on the device.
- **On-device Processing**: No internet connection required. The model is optimized for mobile devices using TensorFlow Lite.
- **Low Bandwidth & Privacy Focused**: Ideal for rural areas with limited internet access and concerns about data privacy.
