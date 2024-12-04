import os
import random
import shutil

classes = {
    "Corn_(maize)___healthy": "Healthy",
    "Apple___healthy": "Healthy",
    "Corn_(maize)___Common_rust_": "Diseased",
    "Corn_(maize)___Northern_Leaf_Blight": "Diseased",
    "Apple___Apple_scab": "Diseased",
    "Apple___Black_rot": "Diseased"
}

source_base_path = "c:\Users\tamar\Downloads\PlantVillage Dataset (Labeled)\Color Images"
target_base_path = "c:\Users\tamar\Downloads\PlantHealthDataset"

num_images = 200

for class_name, target_folder in classes.items():
    source_folder = os.path.join(source_base_path, class_name)
    target_folder = os.path.join(target_base_path, target_folder)

    os.makedirs(target_folder, exist_ok=True)

    image_files = [f for f in os.listdir(source_folder) if f.endswith(('.jpg', '.jpeg', '.png'))]

    selected_files = random.sample(image_files, min(len(image_files), num_images))

    for file_name in selected_files:
        shutil.copy(os.path.join(source_folder, file_name), os.path.join(target_folder, file_name))

    print(f"Copied {len(selected_files)} images from {class_name} to {target_folder}.")
