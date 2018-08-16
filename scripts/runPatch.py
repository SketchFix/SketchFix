import os
import json
import glob
import time
import shutil
import subprocess, shlex
from threading import Timer

def deployProj(projPath, supportPath, patchPath, proj, version):
	tmpDefects4jFolder = "./defects4J_folder"
	cmd = "defects4j checkout -p %s -v %sb -w %s" %(proj, version, tmpDefects4jFolder)
	os.system(cmd)

	defects4jConfig = os.path.join(projPath, "defects4j.build.properties")

	configDict = dict()
	configDict["proj"] = proj
	configDict["version"] = version

	with open(defects4jConfig, 'r') as file:
		for line in file:
			if "d4j.dir.src.classes" in line:
				configDict["classes"] = line.rstrip("\n").split("=")[1]
			if "d4j.dir.src.tests" in line:
				configDict["tests"] = line.rstrip("\n").split("=")[1]

	if proj == "Chart":
		configDict["ant_folder"] = "ant"
		configDict["build_file"] = "ant/build.xml"
	else:
		configDict["ant_folder"] = ""
		configDict["build_file"] = "build.xml"

	configDict["patch_path"] = os.path.join(patchPath, (proj + "_" + version))

	# copy related files
	classesRootPath = configDict["classes"].split("/")[0]


	code_folder_src = os.path.join(tmpDefects4jFolder, classesRootPath)
	code_folder_dst = os.path.join(projPath, classesRootPath)
	shutil.rmtree(code_folder_dst)

	shutil.copytree(code_folder_src, code_folder_dst)

	testDriver_src = os.path.join(supportPath, (proj + "_" + version), "Sketch4JDriver.java")
	testDriver_dst = os.path.join(projPath, configDict["tests"], "Sketch4JDriver.java")
	shutil.copyfile(testDriver_src, testDriver_dst)

	return configDict


def repalceLibPath(inputPath, configDict):
	libPath_org = "\/Users\/lisahua\/projects\/jpf\/example\/workspace\/Sketch4J"
	libPath_new = "\/home\/mengshi\/Lisa_Hua\/libs"
	buildFile = os.path.join(inputPath, configDict["build_file"])
	replacementCMD = "sed -i -e 's/%s/%s/g' %s" %(libPath_org, libPath_new, buildFile)
	# print "RUN:", replacementCMD
	os.system(replacementCMD)

	# if configDict["proj"] == "Chart":
	# 	replacementCMD2 = "sed -i -e 's/%s/%s/g' %s" %('\"1.4\"', '\"1.7\"', buildFile)
	# 	os.system(replacementCMD2)


def runAnt(inputPath, configDict):

	def kill_proc(proc, timeout):
		timeout["value"] = True
		proc.kill()

	def run(cmd, timeout_sec):
		proc = subprocess.Popen(shlex.split(cmd), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
		timeout = {"value": False}
		timer = Timer(timeout_sec, kill_proc, [proc, timeout])
		timer.start()
		stdout, stderr = proc.communicate()
		timer.cancel()
		return proc.returncode, stdout.decode("utf-8"), stderr.decode("utf-8"), timeout["value"]

	patchPath = os.path.join(inputPath, configDict["ant_folder"])
	currentPath = os.getcwd()
	os.chdir(patchPath)
	antCMD = "ant sketch4j"
	returncode, stdout_msg, stderr_msg, timeout_flag = run(antCMD, 180)
	# timeout_flag = False
	# os.system(antCMD)
	# stdout_msg = "GOOD"

	os.chdir(currentPath)

	# print stdout_msg
	# print stderr_msg + "\n\n\n\n\n"

	if timeout_flag:
		print "    Timeout    ",
		return False, None


	elif "[java] Found solution:" in stdout_msg:
		print "    Found solution    ",
		msgList = stdout_msg.split("\n")
		returnList = []
		flag = False
		for msg_i in msgList:
			if " candidates for the" in msg_i:
				flag = True
			if flag and (("Hole" in msg_i) or ("Space:" in msg_i) or (" candidates for the" in msg_i)):
				returnList.append(msg_i)
			if "BUILD SUCCESSFUL" in msg_i:
				break
		return True, returnList


	elif "[java] No solution!" in stdout_msg:
		print "    No solution    ",
		msgList = stdout_msg.split("\n")
		returnList = []
		flag = False
		for msg_i in msgList:
			if " candidates for the" in msg_i:
				flag = True
			if flag and (("Hole" in msg_i) or ("Space:" in msg_i) or (" candidates for the" in msg_i)):
				returnList.append(msg_i)
			if "BUILD SUCCESSFUL" in msg_i:
				break
		return False, returnList


	else:
		return False, None



def testAllPatches(inputPath, configDict):
	patchPath = configDict["patch_path"]
	patchList = glob.glob(os.path.join(patchPath, "*.java*"))
	# print patchList

	validPatchList = []
	validPatchDict = dict()
	validTimeList = []
	nonValidPatchList = []
	nonValidPatchDict = dict()
	nonValidTimeList = []

	counter = 1

	for patch_i in patchList:
		patchName = patch_i.split("/")[-1]
		print "#", counter, 
		javaFileFullName = patch_i.split(".java")[0] + ".java"
		className = javaFileFullName.split("/")[-1]

		# find java file
		javaFilePath = os.path.join(inputPath, configDict["classes"])
		findJavaCMD = 'find %s -wholename "*\\/%s"' %(javaFilePath, className)
		process = os.popen(findJavaCMD)
		javaFileList = process.read().rstrip("\n").split("\n")
		# javaPath = "/".join(javaFile.split("/")[:-1])
		process.close()

		for javaFile in javaFileList:
			# back up target file
			tmpFolder = os.path.join(inputPath, "TMP")
			if os.path.exists(tmpFolder) is False:
				os.mkdir(tmpFolder)
			dstFile = os.path.join(tmpFolder, "tmptmptmp.java")
			shutil.copyfile(javaFile, dstFile)
			# print "org:", javaFile
			# print "new:", dstFile

			# replace java file
			shutil.copyfile(patch_i, javaFile)

			# run sketch4j
			startingTime = time.time()
			flag, returnList = runAnt(inputPath, configDict)
			deltaTime = time.time() - startingTime

			if flag:
				validPatchList.append(patchName)
				validTimeList.append(deltaTime)
				validPatchDict[patchName] = returnList
			else:
				nonValidPatchList.append(patchName)
				nonValidTimeList.append(deltaTime)
				if returnList is not None:
					nonValidPatchDict[patchName] = returnList

			# recover file
			shutil.copyfile(dstFile, javaFile)
			counter += 1
			print "  PATCH NAME:", patchName
			# break


	patchDict = dict()
	patchDict["vpList"] = validPatchList
	patchDict["vpDict"] = validPatchDict
	patchDict["vtList"] = validTimeList
	patchDict["nvpList"] = nonValidPatchList
	patchDict["nvtList"] = nonValidTimeList
	patchDict["nvpDict"] = nonValidPatchDict

	return patchDict


def saveResult(configDict, patchDict):
	rootFolder = "./sketch4j_result"
	if os.path.exists(rootFolder) is False:
		os.mkdir(rootFolder)

	outputFolder = os.path.join(rootFolder, (configDict["proj"] + "_" + configDict["version"]))
	if os.path.exists(outputFolder) is False:
		os.mkdir(outputFolder)

	with open(os.path.join(outputFolder, "valid.txt"), 'w+') as file:
		for i in range(len(patchDict["vpList"])):
			patch_i = patchDict["vpList"][i]
			delta_time_i = patchDict["vtList"][i]
			file.write(patch_i+ " " + str(delta_time_i) + "\n")
			for j in patchDict["vpDict"][patch_i]:
				file.write(j + "\n")
			file.write("\n")

	with open(os.path.join(outputFolder, "nonvalid.txt"), 'w+') as file:
		for i in range(len(patchDict["nvpList"])):
			patch_i = patchDict["nvpList"][i]
			delta_time_i = patchDict["nvpList"][i]
			file.write(patch_i+ " " + str(delta_time_i) + "\n")
			if patch_i in patchDict["nvpDict"]:
				for j in patchDict["nvpDict"][patch_i]:
					file.write(j + "\n")
			file.write("\n")



projDict = dict()
#projDict["Chart"] = [1, 8, 9, 11, 20, 24]
#projDict["Closure"] = [14, 62, 73, 126]
#projDict["Lang"] = [6, 55, 59]
#projDict["Math"] = [5, 33, 59, 70, 82, 85]
#projDict["Time"] = [4, 19]
#projList = ["Chart", "Lang", "Math", "Closure"]

projDict["Math"] = [50]
# projDict["Lang"] = [6, 55, 59]
# projDict["Chart"] = [24]
projList = ["Math"]


patchPath = "./input"
supportPath = "./support"
projRootPath = "./dataset"

for proj_i in projList:
	for ver_j in projDict[proj_i]:

		version = str(ver_j)
		projPath = os.path.join(projRootPath, (proj_i + "_" + version))

		configDict = deployProj(projPath, supportPath, patchPath, proj_i, version)
		repalceLibPath(projPath, configDict)
		patchDict = testAllPatches(projPath, configDict)
		saveResult(configDict, patchDict)
		# break
		print "************************************************"
		print "************************************************"
		print "************************************************"
		print "************************************************"
		print "************************************************"
		print "************************************************"
			
		# try:
		# 	version = str(ver_j)
		# 	projPath = os.path.join(projRootPath, (proj_i + "_" + version))

		# 	configDict = deployProj(projPath, supportPath, patchPath, proj_i, version)
		# 	repalceLibPath(projPath, configDict)
		# 	patchDict = testAllPatches(projPath, configDict)
		# 	saveResult(configDict, patchDict)
		# 	# break
		# 	print "************************************************"
		# 	print "************************************************"
		# 	print "************************************************"
		# 	print "************************************************"
		# 	print "************************************************"
		# 	print "************************************************"

		# except:
		# 	print('An error occured.')

