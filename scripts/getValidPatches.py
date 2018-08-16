import os

def getValidPatches(rootPath, subj_name):
	file_name = os.path.join(rootPath, subj_name, "valid.txt")
	patch_set = set()
	with open(file_name, 'r') as file:
		for line in file:
			if ".java-" in line:
				patch_set.add(line.split(" ")[0])

	return list(patch_set)


def moveFiles(srcPath, subj_name, patchList, dstPath):
	targetFolder = os.path.join(dstPath, subj_name)
	if os.path.exists(targetFolder) is False:
		os.mkdir(targetFolder)

	for patch_i in patchList:
		srcFileName = os.path.join(srcPath, subj_name, patch_i)
		dstFileName = os.path.join(dstPath, subj_name, patch_i)
		cmd = "cp %s %s" %(srcFileName, dstFileName)
		os.system(cmd)
		print patch_i
		# break



projDict = {
	"Chart":["1", "8", "9", "11", "20", "24"],
	"Closure":["14", "62", "126"],
	"Lang":["6", "55", "59"],
	"Math":["5", "33", "50", "59", "70", "82", "85"]
}
rootPath = "./sketch4j_resultbackup"
srcPath = "./input"
dstPath = "./valid_patches"

for proj_i in projDict:
	for ver_j in projDict[proj_i]:
		print "Proj:%s, Version: %s" %(proj_i, ver_j)
		print "================================================"
		subj_name = proj_i + "_" + ver_j
		patchList = getValidPatches(rootPath, subj_name)
		moveFiles(srcPath, subj_name, patchList, dstPath)
		print "================================================"
		# break
	# break
