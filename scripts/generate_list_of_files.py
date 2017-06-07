from os import listdir
import json

files = listdir('./traffic_images')
data = []

# loop over the list of files and write it to an array
for image_file in files:
    row = {}
    row['filename'] = image_file
    row['is_congested'] = -1
    data.append(row)
    
# export data to json file
with open('data.json', 'w') as fp:
    json.dump(data, fp)
