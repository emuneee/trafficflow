import json
import os
import shutil
from PIL import Image

print 'Hello'

## load the json file
data = json.load(open('../trafficflow-export.json'))

## remove old directories
print 'Removing old temp data...'
shutil.rmtree('train/congested', ignore_errors=True, onerror=None)
shutil.rmtree('train/not_congested', ignore_errors=True, onerror=None)
shutil.rmtree('validation/congested', ignore_errors=True, onerror=None)
shutil.rmtree('validation/not_congested', ignore_errors=True, onerror=None)

## create a folder for ditch, not congested, and congested
print 'Creating new directories...'
os.makedirs('validation/congested')
os.makedirs('validation/not_congested')
os.makedirs('train/congested')
os.makedirs('train/not_congested')

## get the last processed index
index = data['index']
print 'Processing will continue through element ', index

# start iterating through the data array
dataArray = data['data']
congestedCount = 0
notCongestedCount = 0
trainToValidationRatio = 5

for x in range(0, index + 1):
	element = dataArray[x]

	if 'isCongested' in element:
		isCongested = element['isCongested']
		filename = element['filename']
		destination = ''

		if isCongested == 1:
			if x % trainToValidationRatio != 0:
				destination = 'train/congested/'
			else:
				destination = 'validation/congested/'
			congestedCount = congestedCount + 1
		elif isCongested == 0:
			if x % trainToValidationRatio != 0:
				destination = 'train/not_congested/'
			else:
				destination = 'validation/not_congested/'
			notCongestedCount = notCongestedCount + 1

		if len(destination) > 0:
			print 'Resizing image', filename
			resizedFilename = filename + '_resized.jpg'

			try:
				im = Image.open('../traffic_images/' + filename)
				imResize = im.resize((150, 150), Image.ANTIALIAS)
				imResize.save(resizedFilename, 'JPEG', quality = 90)
				print 'Copying file', resizedFilename, 'to', destination
				shutil.copyfile(resizedFilename, destination + filename)
				os.remove(resizedFilename)
			except IOError:
				print 'IOError for', filename
		
print 'Number of congested: ', congestedCount
print 'Number of not congested: ', notCongestedCount