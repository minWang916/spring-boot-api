{{/* Generate a unique name using the release name and chart name */}}
{{- define "name" -}}
{{- printf "%s-%s" .Release.Name .Chart.Name -}}
{{- end -}}
