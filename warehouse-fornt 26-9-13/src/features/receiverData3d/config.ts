export type SensorMetric = 'temperature' | 'humidity' | 'ph3'

export interface SensorMetricConfig {
  title: string
  subtitle: string
  label: string
  unit: string
}

export const SENSOR_METRICS: Record<SensorMetric, SensorMetricConfig> = {
  temperature: {
    title: '3D粮仓温度展示',
    subtitle: '仓内温度测点分布',
    label: '温度',
    unit: '℃',
  },
  humidity: {
    title: '3D粮仓湿度展示',
    subtitle: '仓内湿度测点分布',
    label: '湿度',
    unit: '%',
  },
  ph3: {
    title: '3D粮仓 PH3 展示',
    subtitle: '仓内 PH3 测点分布',
    label: 'PH3浓度',
    unit: 'ppm',
  },
}
