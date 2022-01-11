# Copyright (C) 2009 The Android Open Source Project
# Copyright (C) 2019 The Mokee Open Source Project
# Copyright (C) 2019 The LineageOS Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import common
import re

def FullOTA_InstallBegin(info):
  data = info.input_zip.read("RADIO/dynamic-remove-oplus")
  common.ZipWriteStr(info.output_zip, "dynamic-remove-oplus", data)
  info.script.AppendExtra('update_dynamic_partitions(package_extract_file("dynamic-remove-oplus"));')
  return

def FullOTA_InstallEnd(info):
  OTA_InstallEnd(info)
  return

def IncrementalOTA_InstallEnd(info):
  OTA_InstallEnd(info)
  return

def AddImage(info, basename, dest):
  name = basename
  data = info.input_zip.read("IMAGES/" + basename)
  common.ZipWriteStr(info.output_zip, name, data)
  info.script.AppendExtra('package_extract_file("%s", "%s");' % (name, dest))
  
def AddImageRadio(info, basename, dest):
  name = basename
  data = info.input_zip.read("RADIO/" + basename)
  common.ZipWriteStr(info.output_zip, name, data)
  if dest: # Dont append any line in updater-script if dest is not supplied to this definition
    info.script.AppendExtra('package_extract_file("%s", "%s");' % (name, dest))

def OTA_InstallEnd(info):
# Adding vendor,odm and logo to zip and will flash it
  AddImageRadio(info, "dynamic-resize", "")
  AddImageRadio(info, "odm.new.dat.br", "")
  AddImageRadio(info, "odm.patch.dat", "")
  AddImageRadio(info, "odm.transfer.list", "")
  AddImageRadio(info, "vendor.new.dat.br", "")
  AddImageRadio(info, "vendor.patch.dat", "")
  AddImageRadio(info, "vendor.transfer.list", "")
  AddImageRadio(info, "logo.bin", "")
  info.script.Print("Patching odm image unconditionally...")
  info.script.AppendExtra('assert(update_dynamic_partitions(package_extract_file("dynamic-resize")));')
  info.script.AppendExtra('block_image_update(map_partition("odm"), package_extract_file("odm.transfer.list"), "odm.new.dat.br", "odm.patch.dat") || abort("E2001: Failed to update odm image.");')
  info.script.Print("Patching vendor image unconditionally...")
  info.script.AppendExtra('block_image_update(map_partition("vendor"), package_extract_file("vendor.transfer.list"), "vendor.new.dat.br", "vendor.patch.dat") || abort("E2001: Failed to update vendor image.");')
  info.script.Print("Patching firmware images...")
  AddImage(info, "dtbo.img", "/dev/block/by-name/dtbo")
  AddImage(info, "vbmeta.img", "/dev/block/by-name/vbmeta")
  AddImage(info, "vbmeta_system.img", "/dev/block/by-name/vbmeta_system")
  info.script.AppendExtra('package_extract_file("logo.bin", "/dev/block/by-name/logo");')
  return
