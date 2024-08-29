import sys
import nibabel as nib
import numpy as np
import matplotlib.pyplot as plt
import os

def extract_middle_slice(nii_file_path):
    # 读取NII文件
    img = nib.load(nii_file_path)
    data = img.get_fdata()

    # 获取切片的维度
    mid_slice_index = data.shape[2] // 2  # 中间切片的索引

    # 提取中间切片并保存为JPG文件
    mid_slice = data[:, :, mid_slice_index]

    # 将切片标准化到0-255范围
    mid_slice_normalized = (mid_slice - np.min(mid_slice)) / (np.max(mid_slice) - np.min(mid_slice)) * 255
    mid_slice_image = mid_slice_normalized.astype(np.uint8)

    # 保存中间切片为JPG格式
    output_file_path = os.path.splitext(nii_file_path)[0] + '_middle_slice.jpg'
    plt.imsave(output_file_path, mid_slice_image, cmap='gray')

    return output_file_path

if __name__ == "__main__":
    nii_file_path = sys.argv[1]
    output_path = extract_middle_slice(nii_file_path)
    print(f"Middle slice saved to: {output_path}")