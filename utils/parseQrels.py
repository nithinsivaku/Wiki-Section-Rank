# @Author: Nithin Sivakumar <Nithin>
# @Date:   2019-06-25T00:46:42-04:00
# @Last modified by:   Nithin
# @Last modified time: 2019-11-27T21:14:05-05:00

"""
Helper script to query source qrels files and extract
the lines which match the given pages. Writes the
extracted contents onto new qrel files
"""

import os
import sys

class ParseFile():
	def __init__(self, inputFilePath, outputFilePath ):
		self.input_file_location = inputFilePath
		self.output_file_location = outputFilePath
		self.Pages = ["enwiki:Drinking%20bird", "enwiki:Hot%20chocolate", "enwiki:Atmosphere%20of%20Earth", "enwiki:Water%20cycle", "enwiki:Tsunami", "enwiki:Electric%20car", "enwiki:Supernova"]

	def parse(self):
		new_content = []
		with open(self.input_file_location) as f:
			for line in f:
				lineContent = line.strip().split(' ')
				if lineContent[0].split('/')[0] in self.Pages:
					new_line = ' '.join(lineContent)+'\n'
					new_content.append(new_line)
		self.writeToFile(new_content)

	def writeToFile(self, content):
		f= open(self.output_file_location,"w+")
		for item in content:
			f.write(item)
		print("over")


if __name__ == '__main__':
	print(sys.argv[1], sys.argv[2])
	P = ParseFile(sys.argv[1], sys.argv[2])
	P.parse()
