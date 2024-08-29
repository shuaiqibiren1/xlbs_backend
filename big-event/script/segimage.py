import sys
import nibabel as nib
import numpy as np
import matplotlib.pyplot as plt
import os

def extract_multiple_slices(nii_file_path, num_slices=10):
    # 读取NIfTI文件
    img = nib.load(nii_file_path)
    data = img.get_fdata()

    # 获取切片的维度
    num_total_slices = data.shape[2]
    slice_indices = np.linspace(0, num_total_slices - 1, num_slices, dtype=int)

    output_files = []
    for i, slice_index in enumerate(slice_indices):
        # 提取指定切片并保存为JPG文件
        slice_data = data[:, :, slice_index]
        slice_normalized = (slice_data - np.min(slice_data)) / (np.max(slice_data) - np.min(slice_data)) * 255
        slice_image = slice_normalized.astype(np.uint8)

        # 保存切片为JPG格式
        output_file_path = os.path.splitext(nii_file_path)[0] + f'_slice_{i}.jpg'
        plt.imsave(output_file_path, slice_image, cmap='gray')
        output_files.append(output_file_path)

    return output_files

if __name__ == "__main__":
    nii_file_path = sys.argv[1]
    output_paths = extract_multiple_slices(nii_file_path)
    for path in output_paths:
        print(f"Slice saved to: {path}")
