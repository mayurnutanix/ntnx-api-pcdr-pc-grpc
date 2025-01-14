
3.0.1-
PC Protection PC Client SDK0.0.1-SNAPSHOT�
�
M/prism/v4.0.b1/management/domain-managers/{domainManagerExtId}/backup-targets�
"�
DomainManagerBackups"listBackupTargets:��
200�
�
SList of backup clusters/object store backing up the domain manager configurations.
g
e
application/jsonQ
OM
K#/components/schemas/prism.v4.r0.b1.management.ListBackupTargetsApiResponse�
4XX�
�
Client error responseg
e
application/jsonQ
OM
K#/components/schemas/prism.v4.r0.b1.management.ListBackupTargetsApiResponse�
5XX�
�
Server error responseg
e
application/jsonQ
OM
K#/components/schemas/prism.v4.r0.b1.management.ListBackupTargetsApiResponse2�
DomainManagerBackups"createBackupTarget2]
[W
U
application/jsonA
?=
;#/components/schemas/prism.v4.r0.b1.management.BackupTarget:��
202�
�
6Task Id corresponding to the create backup target api
y
w
Locationk
iBg
ezZ^((http[s]?):\/)?\/?([^:\/\s]+)((\/\w+)*(:[0-9]+)*?\/)([\w\-\.]+[^#?\s]+)(.*)?(#[\w\-]+)?$�stringh
f
application/jsonR
PN
L#/components/schemas/prism.v4.r0.b1.management.CreateBackupTargetApiResponse�
4XX�
�
Client error responseh
f
application/jsonR
PN
L#/components/schemas/prism.v4.r0.b1.management.CreateBackupTargetApiResponse�
5XX�
�
Server error responseh
f
application/jsonR
PN
L#/components/schemas/prism.v4.r0.b1.management.CreateBackupTargetApiResponseb�
�
domainManagerExtIdpath :SIMPLE@ RZ
XzM^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$�string
�
U/prism/v4.0.b1/management/domain-managers/{domainManagerExtId}/backup-targets/{extId}�"�
DomainManagerBackups"getBackupTargetById*w
u
extIdpath :SIMPLE@ RZ
XzM^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$�string:��
200�
�
OBackup target details corresponding to the cluster/object store configuration.
e
c
application/jsonO
MK
I#/components/schemas/prism.v4.r0.b1.management.GetBackupTargetApiResponse�
4XX�
~
Client error responsee
c
application/jsonO
MK
I#/components/schemas/prism.v4.r0.b1.management.GetBackupTargetApiResponse�
5XX�
~
Server error responsee
c
application/jsonO
MK
I#/components/schemas/prism.v4.r0.b1.management.GetBackupTargetApiResponse*�
DomainManagerBackups"updateBackupTargetById2]
[W
U
application/jsonA
?=
;#/components/schemas/prism.v4.r0.b1.management.BackupTarget:��
202�
�
7Task Id corresponding to the update backup target api.
y
w
Locationk
iBg
ezZ^((http[s]?):\/)?\/?([^:\/\s]+)((\/\w+)*(:[0-9]+)*?\/)([\w\-\.]+[^#?\s]+)(.*)?(#[\w\-]+)?$�stringh
f
application/jsonR
PN
L#/components/schemas/prism.v4.r0.b1.management.UpdateBackupTargetApiResponse�
4XX�
�
Client error responseh
f
application/jsonR
PN
L#/components/schemas/prism.v4.r0.b1.management.UpdateBackupTargetApiResponse�
5XX�
�
Server error responseh
f
application/jsonR
PN
L#/components/schemas/prism.v4.r0.b1.management.UpdateBackupTargetApiResponse:�
DomainManagerBackups"deleteBackupTargetById:��
202�
�
7Task Id corresponding to the delete backup target api.
y
w
Locationk
iBg
ezZ^((http[s]?):\/)?\/?([^:\/\s]+)((\/\w+)*(:[0-9]+)*?\/)([\w\-\.]+[^#?\s]+)(.*)?(#[\w\-]+)?$�stringh
f
application/jsonR
PN
L#/components/schemas/prism.v4.r0.b1.management.DeleteBackupTargetApiResponse�
4XX�
�
Client error responseh
f
application/jsonR
PN
L#/components/schemas/prism.v4.r0.b1.management.DeleteBackupTargetApiResponse�
5XX�
�
Server error responseh
f
application/jsonR
PN
L#/components/schemas/prism.v4.r0.b1.management.DeleteBackupTargetApiResponseb�
�
domainManagerExtIdpath :SIMPLE@ RZ
XzM^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$�stringbw
u
extIdpath :SIMPLE@ RZ
XzM^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$�string"�N
\
"
basicAuthScheme

HTTP*basic
6
apiKeyAuthScheme"
 
APIKEYX-ntnx-api-key"HEADER:�M
�
6prism.v4.r0.b1.management.ListBackupTargetsApiResponse�
��object��
Q
metadataEC
A#/components/schemas/common.v1.r0.b1.response.ApiResponseMetadata
�
data�
��Q
O��array�A
?=
;#/components/schemas/prism.v4.r0.b1.management.BackupTarget�;9
7#/components/schemas/prism.v4.r0.b1.error.ErrorResponse

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
&prism.v4.r0.b1.management.BackupTarget�
��location�MK
I#/components/schemas/common.v1.r0.b1.response.ExternalizableAbstractModel��
��object��
�
location�
��$objectType�object�B@
>#/components/schemas/prism.v4.r0.b1.management.ClusterLocation�FD
B#/components/schemas/prism.v4.r0.b1.management.ObjectStoreLocation��
$objectTypee
#prism.v4.management.ClusterLocation>#/components/schemas/prism.v4.r0.b1.management.ClusterLocationm
'prism.v4.management.ObjectStoreLocationB#/components/schemas/prism.v4.r0.b1.management.ObjectStoreLocation
(
lastSyncTime
*	date-time�string
 
isBackupPaused
�boolean
"
backupPauseReason
�string� 
�
7prism.v4.r0.b1.management.CreateBackupTargetApiResponse�
��object��
Q
metadataEC
A#/components/schemas/common.v1.r0.b1.response.ApiResponseMetadata
�
data�
��$objectType�<:
8#/components/schemas/prism.v4.r0.b1.config.TaskReference�;9
7#/components/schemas/prism.v4.r0.b1.error.ErrorResponse��
$objectTypeY
prism.v4.config.TaskReference8#/components/schemas/prism.v4.r0.b1.config.TaskReferenceW
prism.v4.error.ErrorResponse7#/components/schemas/prism.v4.r0.b1.error.ErrorResponse

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
)prism.v4.r0.b1.management.ClusterLocation�
��object��
M
configCA
?#/components/schemas/prism.v4.r0.b1.management.ClusterReference

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
-prism.v4.r0.b1.management.ObjectStoreLocation�
��backupPolicy�providerConfig�object��
P
providerConfig><
:#/components/schemas/prism.v4.r0.b1.management.AWSS3Config
O
backupPolicy?=
;#/components/schemas/prism.v4.r0.b1.management.BackupPolicy

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
%prism.v4.r0.b1.management.AWSS3Config�
��;9
7#/components/schemas/prism.v4.r0.b1.management.S3Config��
��object��
�
credentials�
��$objectType�GE
C#/components/schemas/prism.v4.r0.b1.management.AccessKeyCredentials�~
$objectTypeo
(prism.v4.management.AccessKeyCredentialsC#/components/schemas/prism.v4.r0.b1.management.AccessKeyCredentials� 
�
&prism.v4.r0.b1.management.BackupPolicy�
��rpoInMinutes�object��
5
rpoInMinutes%
#*int32I     ��@Y      N@�integer

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
"prism.v4.r0.b1.management.S3Config�
��
bucketName�region�object��


bucketName
h?p�string

region
h?�string

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
.prism.v4.r0.b1.management.AccessKeyCredentials�
��object��

accessKeyId
h�p�string
+
secretAccessKey
*passwordh��string

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
4prism.v4.r0.b1.management.GetBackupTargetApiResponse�
��object��
Q
metadataEC
A#/components/schemas/common.v1.r0.b1.response.ApiResponseMetadata
�
data�
��$objectType�?=
;#/components/schemas/prism.v4.r0.b1.management.BackupTarget�;9
7#/components/schemas/prism.v4.r0.b1.error.ErrorResponse��
$objectType_
 prism.v4.management.BackupTarget;#/components/schemas/prism.v4.r0.b1.management.BackupTargetW
prism.v4.error.ErrorResponse7#/components/schemas/prism.v4.r0.b1.error.ErrorResponse

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
7prism.v4.r0.b1.management.UpdateBackupTargetApiResponse�
��object��
Q
metadataEC
A#/components/schemas/common.v1.r0.b1.response.ApiResponseMetadata
�
data�
��$objectType�<:
8#/components/schemas/prism.v4.r0.b1.config.TaskReference�;9
7#/components/schemas/prism.v4.r0.b1.error.ErrorResponse��
$objectTypeY
prism.v4.config.TaskReference8#/components/schemas/prism.v4.r0.b1.config.TaskReferenceW
prism.v4.error.ErrorResponse7#/components/schemas/prism.v4.r0.b1.error.ErrorResponse

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
7prism.v4.r0.b1.management.DeleteBackupTargetApiResponse�
��object��
Q
metadataEC
A#/components/schemas/common.v1.r0.b1.response.ApiResponseMetadata
�
data�
��$objectType�<:
8#/components/schemas/prism.v4.r0.b1.config.TaskReference�;9
7#/components/schemas/prism.v4.r0.b1.error.ErrorResponse��
$objectTypeY
prism.v4.config.TaskReference8#/components/schemas/prism.v4.r0.b1.config.TaskReferenceW
prism.v4.error.ErrorResponse7#/components/schemas/prism.v4.r0.b1.error.ErrorResponse

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
4common.v1.r0.b1.response.ExternalizableAbstractModel�
��@>
<#/components/schemas/common.v1.r0.b1.config.TenantAwareModel��
��object��
g
extId^
\zM^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$�string
Y
linksP
N�� �array�;
97
5#/components/schemas/common.v1.r0.b1.response.ApiLink� 
�
*prism.v4.r0.b1.management.ClusterReference�
��extId�object��
c
extIdZ
XzM^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$�string

name
hPp�string

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
'common.v1.r0.b1.config.TenantAwareModel�
��object��
h
tenantId\
ZzM^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}$�string

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
 common.v1.r0.b1.response.ApiLink�
��object��

href
	�string

rel
	�string

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
"prism.v4.r0.b1.error.ErrorResponse�
��object��
�
error�
��G
E�array�:
86
4#/components/schemas/prism.v4.r0.b1.error.AppMessage�CA
?#/components/schemas/prism.v4.r0.b1.error.SchemaValidationError

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
prism.v4.r0.b1.error.AppMessage�
��object��

message
	�string
K
severity?=
;#/components/schemas/common.v1.r0.b1.config.MessageSeverity

code
	�string
!
locale
�string�	
en_US


errorGroup
	�string
+
argumentsMap
�object�

	�string

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
m
$prism.v4.r0.b1.error.MessageSeverityE
C�
$UNKNOWN�	$REDACTED�INFO�	WARNING�ERROR�string
�
*prism.v4.r0.b1.error.SchemaValidationError�
��object��
#
	timestamp
*	date-time�string


statusCode

�integer

error
	�string

path
	�string
t
validationErrorMessagesY
W�array�L
JH
F#/components/schemas/prism.v4.r0.b1.error.SchemaValidationErrorMessage

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
1prism.v4.r0.b1.error.SchemaValidationErrorMessage�
��object��

location
	�string

message
	�string

attributePath
	�string

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
,common.v1.r0.b1.response.ApiResponseMetadata�
��object��
R
flagsI
G�� �array�6
42
0#/components/schemas/common.v1.r0.b1.config.Flag
W
linksN
L�� �array�;
97
5#/components/schemas/common.v1.r0.b1.response.ApiLink
,
totalAvailableResults
*int32�integer
X
messagesL
J�� �array�9
75
3#/components/schemas/common.v1.r0.b1.config.Message
X
	extraInfoK
I�� �array�8
64
2#/components/schemas/common.v1.r0.b1.config.KVPair

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
#prism.v4.r0.b1.config.TaskReference�
��object��
w
extIdn
lza^[a-zA-Z0-9/+]*={0,2}:[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}�string

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
common.v1.r0.b1.config.Flag�
��object��

name
h�p�string

value
�boolean�
 

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
common.v1.r0.b1.config.Message�
��object��

code
	�string

message
	�string
!
locale
�string�	
en_US
K
severity?=
;#/components/schemas/common.v1.r0.b1.config.MessageSeverity

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
�
common.v1.r0.b1.config.KVPair�
��object��

name
h�p�string
�
value�
��
	�string�

�integer�

�boolean� 
�d� �array�

	�string�
�object�

	�string�W
U�� �array�D
B@
>#/components/schemas/common.v1.r0.b1.config.MapOfStringWrapper�!
�d� �array�


�integer

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� 
o
&common.v1.r0.b1.config.MessageSeverityE
C�
$UNKNOWN�	$REDACTED�INFO�	WARNING�ERROR�string
�
)common.v1.r0.b1.config.MapOfStringWrapper�
��object��
"
map
�object�

	�string

	$reserved
�object�

$objectType
	�string
"
$unknownFields
�object�� *

basicAuthScheme *

apiKeyAuthScheme 