/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 * Author: deepanshu.jain@nutanix.com
 *
 * This utility provides utility functions for managing root certificates and creating JWT tokens.
 *
 */

package utils

import (
	"crypto"
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha256"
	"crypto/x509"
	"encoding/base64"
	"encoding/json"
	"encoding/pem"
	"errors"
	"fmt"
	"os"
	"strings"
	"time"

	"ntnx-api-guru-pc/guru-pc-api-service/consts"
	"ntnx-api-guru-pc/guru-pc-api-service/interfaces/external"

	genesisSvc "github.com/nutanix-core/go-cache/infrastructure/cluster/client/genesis"

	log "github.com/sirupsen/logrus"
)

var OsReadFileFunc = os.ReadFile

// GetCertChain creates the certificate chain using the entity cert (guru), ICA, and root certificates.
func GetCAChain() (rootCert *string, certChain []string, err error) {
	entityCert, err := ReadFile(consts.EntityCertPath)
	if err != nil {
		return &consts.EmptyString, nil, fmt.Errorf("reading entity cert: %w", err)
	}

	icaCert, err := ReadFile(consts.IcaCertPath)
	if err != nil {
		return &consts.EmptyString, nil, fmt.Errorf("reading ica cert: %w", err)
	}

	rootCert, err = ReadFile(consts.RootCertPath)
	if err != nil {
		return &consts.EmptyString, nil, fmt.Errorf("reading root cert: %w", err)
	}

	certChain = []string{*entityCert, *icaCert, *rootCert}
	return rootCert, certChain, nil
}

// ReadFile reads the content of a file.
func ReadFile(path string) (*string, error) {
	data, err := OsReadFileFunc(path)
	if err != nil {
		log.Errorf("Error while reading file %s: %s", path, err)
		return &consts.EmptyString, err
	}
	content := string(data)
	return &content, nil
}

// Creates a JWT token for the v3 root-cert API. The main difference is with the base64 encoding of the strings.
// V3 API expects Standard Base64 encoding and not URL Base64 encoding.
func CreateV3Jwt(privateKeyPath string) (string, error) {
	privateKeyPem, err := ReadFile(privateKeyPath)
	if err != nil {
		log.Debugf("error reading private key file: %v", err)
		return "", err
	}
	block, _ := pem.Decode([]byte(*privateKeyPem))
	if block == nil {
		log.Debugf("error decoding private key")
		return "", consts.ErrorPrivateKeyDecode
	}
	privateKey, err := x509.ParsePKCS8PrivateKey(block.Bytes)
	if err != nil {
		log.Debugf("error parsing private key: %v", err)
		return "", err
	}

	header := make(map[string]string)
	header["alg"] = "RS256"
	header["typ"] = "JWT"

	payload := make(map[string]int64)
	payload["expiration_time"] = time.Now().Add(time.Second * 180).Unix()

	headerJson, err := json.Marshal(header)
	if err != nil {
		log.Debugf("error marshalling jwt header: %v", err)
		return "", err
	}

	payloadJson, err := json.Marshal(payload)
	if err != nil {
		log.Debugf("error marshalling jwt payload: %v", err)
		return "", err
	}

	headerB64 := base64.StdEncoding.EncodeToString(headerJson)
	payloadB64 := base64.StdEncoding.EncodeToString(payloadJson)

	jwtData := headerB64 + "." + payloadB64

	hash := sha256.New()
	hash.Write([]byte(jwtData))
	digest := hash.Sum(nil)

	signature, err := rsa.SignPSS(rand.Reader, privateKey.(*rsa.PrivateKey), crypto.SHA256, digest, nil)
	if err != nil {
		log.Debugf("error while signing jwt: %v", err)
		return "", err
	}

	signatureB64 := base64.StdEncoding.EncodeToString(signature)

	return (headerB64 + "." + payloadB64 + "." + signatureB64), nil
}

func GetRootCert(clusterUuid string) (string, error) {
	log.Debugf("fetching root cert of pc: %s", clusterUuid)
	args := genesisSvc.GetCACertificateArg{
		ClusterUuid: []byte(clusterUuid),
	}
	ret, err := external.Interfaces().GenesisClient().GetCACertificate(&args)
	if err != nil {
		log.Errorf("Error fetching remote pc %s root cert using genesis: %v", clusterUuid, err)
		return "", err
	}
	cert, err := base64.StdEncoding.DecodeString(string(ret.GetCaCert().GetCaCertificate()))
	if err != nil {
		log.Errorf("error decoding root cert: %v", err)
		return "", err
	}
	log.Debugf("fetched root cert of pc: %s", clusterUuid)
	return string(cert), nil
}

func DecodeJwt(token string) (map[string]interface{}, error) {
	tokenList := strings.Split(token, ".")
	if len(tokenList) < 3 {
		return nil, errors.New("invalid service token")
	}

	decodedJwt, err := base64.RawURLEncoding.DecodeString(tokenList[1])
	if err != nil {
		return nil, errors.New("token decode failed")
	}

	var result map[string]interface{}
	err = json.Unmarshal(decodedJwt, &result)
	if err != nil {
		return nil, err
	}

	return result, nil
}
