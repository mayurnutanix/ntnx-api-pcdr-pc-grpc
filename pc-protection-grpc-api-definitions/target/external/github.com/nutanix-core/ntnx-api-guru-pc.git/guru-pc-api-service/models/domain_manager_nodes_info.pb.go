// Code generated by protoc-gen-go. DO NOT EDIT.
// versions:
// 	protoc-gen-go v1.34.2
// 	protoc        v3.12.1
// source: domain_manager_nodes_info.proto

package models

import (
	protoreflect "google.golang.org/protobuf/reflect/protoreflect"
	protoimpl "google.golang.org/protobuf/runtime/protoimpl"
	reflect "reflect"
	sync "sync"
)

const (
	// Verify that this generated code is sufficiently up-to-date.
	_ = protoimpl.EnforceVersion(20 - protoimpl.MinVersion)
	// Verify that runtime/protoimpl is sufficiently up-to-date.
	_ = protoimpl.EnforceVersion(protoimpl.MaxVersion - 20)
)

type VmSpecs struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	VmName            *string `protobuf:"bytes,1,opt,name=vm_name,json=vmName" json:"vm_name,omitempty"`
	Uuid              *string `protobuf:"bytes,2,opt,name=uuid" json:"uuid,omitempty"`
	ContainerExtId    *string `protobuf:"bytes,3,opt,name=container_ext_id,json=containerExtId" json:"container_ext_id,omitempty"`
	NumVcpus          *int64  `protobuf:"varint,4,opt,name=num_vcpus,json=numVcpus" json:"num_vcpus,omitempty"`
	MemorySizeBytes   *int64  `protobuf:"varint,5,opt,name=memory_size_bytes,json=memorySizeBytes" json:"memory_size_bytes,omitempty"`
	DataDiskSizeBytes *int64  `protobuf:"varint,6,opt,name=data_disk_size_bytes,json=dataDiskSizeBytes" json:"data_disk_size_bytes,omitempty"`
}

func (x *VmSpecs) Reset() {
	*x = VmSpecs{}
	if protoimpl.UnsafeEnabled {
		mi := &file_domain_manager_nodes_info_proto_msgTypes[0]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *VmSpecs) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*VmSpecs) ProtoMessage() {}

func (x *VmSpecs) ProtoReflect() protoreflect.Message {
	mi := &file_domain_manager_nodes_info_proto_msgTypes[0]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use VmSpecs.ProtoReflect.Descriptor instead.
func (*VmSpecs) Descriptor() ([]byte, []int) {
	return file_domain_manager_nodes_info_proto_rawDescGZIP(), []int{0}
}

func (x *VmSpecs) GetVmName() string {
	if x != nil && x.VmName != nil {
		return *x.VmName
	}
	return ""
}

func (x *VmSpecs) GetUuid() string {
	if x != nil && x.Uuid != nil {
		return *x.Uuid
	}
	return ""
}

func (x *VmSpecs) GetContainerExtId() string {
	if x != nil && x.ContainerExtId != nil {
		return *x.ContainerExtId
	}
	return ""
}

func (x *VmSpecs) GetNumVcpus() int64 {
	if x != nil && x.NumVcpus != nil {
		return *x.NumVcpus
	}
	return 0
}

func (x *VmSpecs) GetMemorySizeBytes() int64 {
	if x != nil && x.MemorySizeBytes != nil {
		return *x.MemorySizeBytes
	}
	return 0
}

func (x *VmSpecs) GetDataDiskSizeBytes() int64 {
	if x != nil && x.DataDiskSizeBytes != nil {
		return *x.DataDiskSizeBytes
	}
	return 0
}

type VmDetails struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	VmSpecs *VmSpecs `protobuf:"bytes,1,opt,name=vm_specs,json=vmSpecs" json:"vm_specs,omitempty"`
}

func (x *VmDetails) Reset() {
	*x = VmDetails{}
	if protoimpl.UnsafeEnabled {
		mi := &file_domain_manager_nodes_info_proto_msgTypes[1]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *VmDetails) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*VmDetails) ProtoMessage() {}

func (x *VmDetails) ProtoReflect() protoreflect.Message {
	mi := &file_domain_manager_nodes_info_proto_msgTypes[1]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use VmDetails.ProtoReflect.Descriptor instead.
func (*VmDetails) Descriptor() ([]byte, []int) {
	return file_domain_manager_nodes_info_proto_rawDescGZIP(), []int{1}
}

func (x *VmDetails) GetVmSpecs() *VmSpecs {
	if x != nil {
		return x.VmSpecs
	}
	return nil
}

type DomainManagerNodesInfo struct {
	state         protoimpl.MessageState
	sizeCache     protoimpl.SizeCache
	unknownFields protoimpl.UnknownFields

	VmDetailsList []*VmDetails `protobuf:"bytes,1,rep,name=vm_details_list,json=vmDetailsList" json:"vm_details_list,omitempty"`
	NetworkExtId  *string      `protobuf:"bytes,2,opt,name=network_ext_id,json=networkExtId" json:"network_ext_id,omitempty"`
}

func (x *DomainManagerNodesInfo) Reset() {
	*x = DomainManagerNodesInfo{}
	if protoimpl.UnsafeEnabled {
		mi := &file_domain_manager_nodes_info_proto_msgTypes[2]
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		ms.StoreMessageInfo(mi)
	}
}

func (x *DomainManagerNodesInfo) String() string {
	return protoimpl.X.MessageStringOf(x)
}

func (*DomainManagerNodesInfo) ProtoMessage() {}

func (x *DomainManagerNodesInfo) ProtoReflect() protoreflect.Message {
	mi := &file_domain_manager_nodes_info_proto_msgTypes[2]
	if protoimpl.UnsafeEnabled && x != nil {
		ms := protoimpl.X.MessageStateOf(protoimpl.Pointer(x))
		if ms.LoadMessageInfo() == nil {
			ms.StoreMessageInfo(mi)
		}
		return ms
	}
	return mi.MessageOf(x)
}

// Deprecated: Use DomainManagerNodesInfo.ProtoReflect.Descriptor instead.
func (*DomainManagerNodesInfo) Descriptor() ([]byte, []int) {
	return file_domain_manager_nodes_info_proto_rawDescGZIP(), []int{2}
}

func (x *DomainManagerNodesInfo) GetVmDetailsList() []*VmDetails {
	if x != nil {
		return x.VmDetailsList
	}
	return nil
}

func (x *DomainManagerNodesInfo) GetNetworkExtId() string {
	if x != nil && x.NetworkExtId != nil {
		return *x.NetworkExtId
	}
	return ""
}

var File_domain_manager_nodes_info_proto protoreflect.FileDescriptor

var file_domain_manager_nodes_info_proto_rawDesc = []byte{
	0x0a, 0x1f, 0x64, 0x6f, 0x6d, 0x61, 0x69, 0x6e, 0x5f, 0x6d, 0x61, 0x6e, 0x61, 0x67, 0x65, 0x72,
	0x5f, 0x6e, 0x6f, 0x64, 0x65, 0x73, 0x5f, 0x69, 0x6e, 0x66, 0x6f, 0x2e, 0x70, 0x72, 0x6f, 0x74,
	0x6f, 0x12, 0x06, 0x6d, 0x6f, 0x64, 0x65, 0x6c, 0x73, 0x22, 0xda, 0x01, 0x0a, 0x07, 0x56, 0x6d,
	0x53, 0x70, 0x65, 0x63, 0x73, 0x12, 0x17, 0x0a, 0x07, 0x76, 0x6d, 0x5f, 0x6e, 0x61, 0x6d, 0x65,
	0x18, 0x01, 0x20, 0x01, 0x28, 0x09, 0x52, 0x06, 0x76, 0x6d, 0x4e, 0x61, 0x6d, 0x65, 0x12, 0x12,
	0x0a, 0x04, 0x75, 0x75, 0x69, 0x64, 0x18, 0x02, 0x20, 0x01, 0x28, 0x09, 0x52, 0x04, 0x75, 0x75,
	0x69, 0x64, 0x12, 0x28, 0x0a, 0x10, 0x63, 0x6f, 0x6e, 0x74, 0x61, 0x69, 0x6e, 0x65, 0x72, 0x5f,
	0x65, 0x78, 0x74, 0x5f, 0x69, 0x64, 0x18, 0x03, 0x20, 0x01, 0x28, 0x09, 0x52, 0x0e, 0x63, 0x6f,
	0x6e, 0x74, 0x61, 0x69, 0x6e, 0x65, 0x72, 0x45, 0x78, 0x74, 0x49, 0x64, 0x12, 0x1b, 0x0a, 0x09,
	0x6e, 0x75, 0x6d, 0x5f, 0x76, 0x63, 0x70, 0x75, 0x73, 0x18, 0x04, 0x20, 0x01, 0x28, 0x03, 0x52,
	0x08, 0x6e, 0x75, 0x6d, 0x56, 0x63, 0x70, 0x75, 0x73, 0x12, 0x2a, 0x0a, 0x11, 0x6d, 0x65, 0x6d,
	0x6f, 0x72, 0x79, 0x5f, 0x73, 0x69, 0x7a, 0x65, 0x5f, 0x62, 0x79, 0x74, 0x65, 0x73, 0x18, 0x05,
	0x20, 0x01, 0x28, 0x03, 0x52, 0x0f, 0x6d, 0x65, 0x6d, 0x6f, 0x72, 0x79, 0x53, 0x69, 0x7a, 0x65,
	0x42, 0x79, 0x74, 0x65, 0x73, 0x12, 0x2f, 0x0a, 0x14, 0x64, 0x61, 0x74, 0x61, 0x5f, 0x64, 0x69,
	0x73, 0x6b, 0x5f, 0x73, 0x69, 0x7a, 0x65, 0x5f, 0x62, 0x79, 0x74, 0x65, 0x73, 0x18, 0x06, 0x20,
	0x01, 0x28, 0x03, 0x52, 0x11, 0x64, 0x61, 0x74, 0x61, 0x44, 0x69, 0x73, 0x6b, 0x53, 0x69, 0x7a,
	0x65, 0x42, 0x79, 0x74, 0x65, 0x73, 0x22, 0x37, 0x0a, 0x09, 0x56, 0x6d, 0x44, 0x65, 0x74, 0x61,
	0x69, 0x6c, 0x73, 0x12, 0x2a, 0x0a, 0x08, 0x76, 0x6d, 0x5f, 0x73, 0x70, 0x65, 0x63, 0x73, 0x18,
	0x01, 0x20, 0x01, 0x28, 0x0b, 0x32, 0x0f, 0x2e, 0x6d, 0x6f, 0x64, 0x65, 0x6c, 0x73, 0x2e, 0x56,
	0x6d, 0x53, 0x70, 0x65, 0x63, 0x73, 0x52, 0x07, 0x76, 0x6d, 0x53, 0x70, 0x65, 0x63, 0x73, 0x22,
	0x79, 0x0a, 0x16, 0x44, 0x6f, 0x6d, 0x61, 0x69, 0x6e, 0x4d, 0x61, 0x6e, 0x61, 0x67, 0x65, 0x72,
	0x4e, 0x6f, 0x64, 0x65, 0x73, 0x49, 0x6e, 0x66, 0x6f, 0x12, 0x39, 0x0a, 0x0f, 0x76, 0x6d, 0x5f,
	0x64, 0x65, 0x74, 0x61, 0x69, 0x6c, 0x73, 0x5f, 0x6c, 0x69, 0x73, 0x74, 0x18, 0x01, 0x20, 0x03,
	0x28, 0x0b, 0x32, 0x11, 0x2e, 0x6d, 0x6f, 0x64, 0x65, 0x6c, 0x73, 0x2e, 0x56, 0x6d, 0x44, 0x65,
	0x74, 0x61, 0x69, 0x6c, 0x73, 0x52, 0x0d, 0x76, 0x6d, 0x44, 0x65, 0x74, 0x61, 0x69, 0x6c, 0x73,
	0x4c, 0x69, 0x73, 0x74, 0x12, 0x24, 0x0a, 0x0e, 0x6e, 0x65, 0x74, 0x77, 0x6f, 0x72, 0x6b, 0x5f,
	0x65, 0x78, 0x74, 0x5f, 0x69, 0x64, 0x18, 0x02, 0x20, 0x01, 0x28, 0x09, 0x52, 0x0c, 0x6e, 0x65,
	0x74, 0x77, 0x6f, 0x72, 0x6b, 0x45, 0x78, 0x74, 0x49, 0x64, 0x42, 0x03, 0x5a, 0x01, 0x2e,
}

var (
	file_domain_manager_nodes_info_proto_rawDescOnce sync.Once
	file_domain_manager_nodes_info_proto_rawDescData = file_domain_manager_nodes_info_proto_rawDesc
)

func file_domain_manager_nodes_info_proto_rawDescGZIP() []byte {
	file_domain_manager_nodes_info_proto_rawDescOnce.Do(func() {
		file_domain_manager_nodes_info_proto_rawDescData = protoimpl.X.CompressGZIP(file_domain_manager_nodes_info_proto_rawDescData)
	})
	return file_domain_manager_nodes_info_proto_rawDescData
}

var file_domain_manager_nodes_info_proto_msgTypes = make([]protoimpl.MessageInfo, 3)
var file_domain_manager_nodes_info_proto_goTypes = []any{
	(*VmSpecs)(nil),                // 0: models.VmSpecs
	(*VmDetails)(nil),              // 1: models.VmDetails
	(*DomainManagerNodesInfo)(nil), // 2: models.DomainManagerNodesInfo
}
var file_domain_manager_nodes_info_proto_depIdxs = []int32{
	0, // 0: models.VmDetails.vm_specs:type_name -> models.VmSpecs
	1, // 1: models.DomainManagerNodesInfo.vm_details_list:type_name -> models.VmDetails
	2, // [2:2] is the sub-list for method output_type
	2, // [2:2] is the sub-list for method input_type
	2, // [2:2] is the sub-list for extension type_name
	2, // [2:2] is the sub-list for extension extendee
	0, // [0:2] is the sub-list for field type_name
}

func init() { file_domain_manager_nodes_info_proto_init() }
func file_domain_manager_nodes_info_proto_init() {
	if File_domain_manager_nodes_info_proto != nil {
		return
	}
	if !protoimpl.UnsafeEnabled {
		file_domain_manager_nodes_info_proto_msgTypes[0].Exporter = func(v any, i int) any {
			switch v := v.(*VmSpecs); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
		file_domain_manager_nodes_info_proto_msgTypes[1].Exporter = func(v any, i int) any {
			switch v := v.(*VmDetails); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
		file_domain_manager_nodes_info_proto_msgTypes[2].Exporter = func(v any, i int) any {
			switch v := v.(*DomainManagerNodesInfo); i {
			case 0:
				return &v.state
			case 1:
				return &v.sizeCache
			case 2:
				return &v.unknownFields
			default:
				return nil
			}
		}
	}
	type x struct{}
	out := protoimpl.TypeBuilder{
		File: protoimpl.DescBuilder{
			GoPackagePath: reflect.TypeOf(x{}).PkgPath(),
			RawDescriptor: file_domain_manager_nodes_info_proto_rawDesc,
			NumEnums:      0,
			NumMessages:   3,
			NumExtensions: 0,
			NumServices:   0,
		},
		GoTypes:           file_domain_manager_nodes_info_proto_goTypes,
		DependencyIndexes: file_domain_manager_nodes_info_proto_depIdxs,
		MessageInfos:      file_domain_manager_nodes_info_proto_msgTypes,
	}.Build()
	File_domain_manager_nodes_info_proto = out.File
	file_domain_manager_nodes_info_proto_rawDesc = nil
	file_domain_manager_nodes_info_proto_goTypes = nil
	file_domain_manager_nodes_info_proto_depIdxs = nil
}
