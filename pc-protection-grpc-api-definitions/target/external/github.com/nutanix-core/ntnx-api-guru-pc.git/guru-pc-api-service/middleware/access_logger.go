/*
 * Copyright (c) 2024 Nutanix Inc. All rights reserved.
 * Author: kshitij.kumar@nutanix.com
 * This file contains utilities for logging the ingress requests to the grpc server
 * along with request latency, response time and request ID.
 */

package middleware

import (
	"io"
	"os"
	"path/filepath"
	"strconv"
	"strings"
	"time"

	log "github.com/sirupsen/logrus"
	"google.golang.org/grpc/status"
	"gopkg.in/natefinch/lumberjack.v2"

	"ntnx-api-guru-pc/guru-pc-api-service/consts"
)

// logFormatterParams holds request attributes that will be used for logging
type logFormatterParams struct {
	contextId    string
	hostName     string
	username     string
	startTime    string
	endTime      string
	method       string
	statusCode   string
	latencyMs    string
	responseSize int
}

type AccessLoggerIfc interface {
	WriteToLog(string, time.Time, error, string, string, string, int)
	Close()
}

type AccessLogger struct {
	logger *log.Logger
}

// constructLogStatement : constructs the log statements using params specified above.
func constructLogStatement(params logFormatterParams) []byte {
	var buf strings.Builder
	buf.WriteString(params.hostName)
	buf.WriteString(" ")
	buf.WriteString(params.username)
	buf.WriteString(" [")
	buf.WriteString(params.contextId)
	buf.WriteString("] ")
	buf.WriteString(params.startTime)
	buf.WriteString(" ")
	buf.WriteString(params.method)
	buf.WriteString(" ")
	buf.WriteString(params.statusCode)
	buf.WriteString(" ")
	buf.WriteString(strconv.Itoa(params.responseSize))
	buf.WriteString(" ")
	buf.WriteString(params.latencyMs)
	buf.WriteString("\n")
	return []byte(buf.String())
}

var (
	accessLogFile = filepath.Join(os.Getenv(consts.LogDirEnvVar), consts.AccessFileName)
)

func NewAccessLogger() AccessLoggerIfc {
	l := &lumberjack.Logger{
		Filename:   accessLogFile,
		MaxSize:    1,  // minimum size that can be set is 1 MB
		MaxBackups: 10, //as directed by API infra team.
		LocalTime:  true,
	}

	// Set up writer to write to the lumberjack logger
	fileWriter := io.Writer(l)
	logger := log.New()
	logger.SetOutput(fileWriter)
	return &AccessLogger{logger: logger}
}

// WriteToLog - writes log using io.Writer() instance of access logger.
func (a *AccessLogger) WriteToLog(
	fullMethod string,
	startTime time.Time,
	err error,
	contextId string,
	userName string,
	hostName string,
	responseSize int,
) {
	completionTime := time.Now()
	timeTaken := time.Since(startTime)
	// Initialise the status code for success.
	returnCode, _ := status.FromError(err)

	params := logFormatterParams{
		startTime:    startTime.Format("2006-01-02 15:04:05"),
		endTime:      completionTime.Format("2006-01-02 15:04:05"),
		method:       fullMethod,
		statusCode:   returnCode.String(),
		latencyMs:    timeTaken.String(),
		contextId:    contextId,
		hostName:     hostName,
		username:     userName,
		responseSize: responseSize,
	}

	logStatement := constructLogStatement(params)
	if a.logger == nil {
		log.Errorf("access logger is nil")
		return
	}

	_, err = a.logger.Writer().Write(logStatement)
	if err != nil {
		log.Errorf("Failed to write the access log with error %v", err)
	}
}

func (a *AccessLogger) Close() {
	// explicitly close the writer.
	a.logger.Writer().Close()
}
