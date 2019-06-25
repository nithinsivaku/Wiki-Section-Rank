import os
import sys

class ParseFile():
	def __init__(self, inputFilePath, outputFilePath ):
		self.input_file_location = inputFilePath
		self.output_file_location = outputFilePath
		self.Pages = ["enwiki:Antibiotics", "enwiki:Antimicrobial%20resistance", "enwiki:Antioxidant", "enwiki:Desertification",
					"enwiki:Deforestation"]

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