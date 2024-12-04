import os
dataset_path = "D:\\AAU-projects\\ML\\PlantHealthDataset" 

for category in ["Healthy", "Diseased"]:
    folder_path = os.path.join(dataset_path, category)
    files = os.listdir(folder_path)
    
    for idx, file in enumerate(files):
        if file.endswith(('.jpg', '.jpeg', '.png', '.JPG', '.JPEG', '.PNG')):
            new_name = f"{category}_{idx+1:03}.jpg"
            old_file_path = os.path.join(folder_path, file)
            new_file_path = os.path.join(folder_path, new_name)
            
            os.rename(old_file_path, new_file_path)
            print(f"Renamed: {file} -> {new_name}")

print("Renaming done!")
