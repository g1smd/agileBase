/**
 * Based on Gray theme for Highcharts JS
 * @author Torstein Hønsi
 */

Highcharts.theme = {
	colors: ["#009FE3", "#EC00BC", "#8DC63F", "#656766"],
	chart: {
		borderWidth: 0,
		borderRadius: 15,
		plotBackgroundColor: null,
		plotShadow: false,
		plotBorderWidth: 0
	},
	title: {
		style: {
			color: '#656766',
			font: '10pt Verdana, Geneva, Arial, sans-serif'
		}
	},
	subtitle: {
		style: {
			color: '#656766',
			font: '9pt Verdana, Geneva, Arial, sans-serif'
		}
	},
	xAxis: {
		gridLineWidth: 0,
		lineColor: '#656766',
		tickColor: '#656766',
		labels: {
			style: {
				color: '#656766',
				fontWeight: 'bold'
			}
		},
		title: {
			style: {
				color: '#656766',
				font: 'bold 8pt Verdana, Geneva, Arial, sans-serif'
			}
		}
	},
	yAxis: {
		alternateGridColor: null,
		minorTickInterval: null,
		gridLineColor: '#E7F7F9',
		lineWidth: 0,
		tickWidth: 0,
		labels: {
			style: {
				color: '#656766',
				fontWeight: 'bold'
			}
		},
		title: {
			style: {
				color: '#656766',
				font: 'bold 8pt Verdana, Geneva, Arial, sans-serif'
			}
		}
	},
	legend: {
		itemStyle: {
			color: '#656766'
		},
		itemHoverStyle: {
			color: '#009FE3'
		},
		itemHiddenStyle: {
			color: '#E8E7E3'
		}
	},
	labels: {
		style: {
			color: '#656766'
		}
	},
	tooltip: {
		backgroundColor: '#E8E7E3',
		borderWidth: 0,
		style: {
			color: '#676566'
		}
	},


	plotOptions: {
		line: {
			dataLabels: {
				color: '#656766'
			},
			marker: {
				lineColor: '#656766'
			}
		},
		spline: {
			marker: {
				lineColor: '#656766'
			}
		},
		scatter: {
			marker: {
				lineColor: '#656766'
			}
		},
		candlestick: {
			lineColor: '#656766'
		}
	},

	toolbar: {
		itemStyle: {
			color: '#656766'
		}
	},

	navigation: {
		buttonOptions: {
			backgroundColor: ''#E8E7E3',
			borderColor: '#656766',
			symbolStroke: '#C0C0C0',
			hoverSymbolStroke: '#FFFFFF'
		}
	},

	exporting: {
		buttons: {
			exportButton: {
				symbolFill: '#009FE3'
			},
			printButton: {
				symbolFill: '#009FE3'
			}
		}
	},

	// scroll charts
	rangeSelector: {
		buttonTheme: {
			fill: {
				linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
				stops: [
					[0.4, '#888'],
					[0.6, '#555']
				]
			},
			stroke: '#000000',
			style: {
				color: '#CCC',
				fontWeight: 'bold'
			},
			states: {
				hover: {
					fill: {
						linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
						stops: [
							[0.4, '#BBB'],
							[0.6, '#888']
						]
					},
					stroke: '#000000',
					style: {
						color: 'white'
					}
				},
				select: {
					fill: {
						linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
						stops: [
							[0.1, '#000'],
							[0.3, '#333']
						]
					},
					stroke: '#000000',
					style: {
						color: 'yellow'
					}
				}
			}
		},
		inputStyle: {
			backgroundColor: '#333',
			color: 'silver'
		},
		labelStyle: {
			color: 'silver'
		}
	},

	navigator: {
		handles: {
			backgroundColor: '#666',
			borderColor: '#AAA'
		},
		outlineColor: '#CCC',
		maskFill: 'rgba(16, 16, 16, 0.5)',
		series: {
			color: '#7798BF',
			lineColor: '#A6C7ED'
		}
	},

	scrollbar: {
		barBackgroundColor: {
				linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
				stops: [
					[0.4, '#888'],
					[0.6, '#555']
				]
			},
		barBorderColor: '#CCC',
		buttonArrowColor: '#CCC',
		buttonBackgroundColor: {
				linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
				stops: [
					[0.4, '#888'],
					[0.6, '#555']
				]
			},
		buttonBorderColor: '#CCC',
		rifleColor: '#FFF',
		trackBackgroundColor: {
			linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
			stops: [
				[0, '#000'],
				[1, '#333']
			]
		},
		trackBorderColor: '#666'
	},

	// special colors for some of the demo examples
	legendBackgroundColor: '#E8E7E3',
	legendBackgroundColorSolid: '#E8E7E3',
	dataLabelsColor: '#656766',
	textColor: '#656766',
	maskColor: 'rgba(255,255,255,0.3)'
};

// Apply the theme
var highchartsOptions = Highcharts.setOptions(Highcharts.theme);
