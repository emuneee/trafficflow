import sys
import urllib
import time

url = sys.argv[1]
directory = sys.argv[2]
destination = directory + "/" + str(time.time()) + ".jpg"

print 'Saving image from url ', url, ' to ', destination
urllib.urlretrieve(url, destination)