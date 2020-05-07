class MapComponent {

  constructor(targetElementId) {
    this.targetElementId = targetElementId;

    this.quadrantFeatureStyle = MapComponent._getStyle('rgba(5, 5, 5, 0.2)')
    this.blockFeatureStyle = MapComponent._getStyle('rgba(255, 255, 255, 0.6)')
    this.fieldFeatureStyle = MapComponent._getStyle('rgba(255, 0, 0, 0.6)')
    this.pipelineFeatureStyle = MapComponent._getStyle('rgba(255, 0, 0, 0.6)', 'black', 1.5)

    this.quadrantLayer = new ol.layer.Vector({
      title: 'Quadrants',
      source: MapComponent._getOgaVectorSource('https://opendata.arcgis.com/datasets/36c66e1781b24c0abe945168c99867ef_0.geojson'),
      style: feature => {
        this.quadrantFeatureStyle.getText().setText(feature.get('QUAD_NO'));
        return this.quadrantFeatureStyle;
      }
    });

    this.blockLayer = new ol.layer.Vector({
      title: 'Blocks',
      source: MapComponent._getOgaVectorSource('https://opendata.arcgis.com/datasets/2d61f4a83f2c49b68fcb1b809978bd63_1.geojson'),
      style: feature => {
        this.blockFeatureStyle.getText().setText(feature.get('BLOCKREF'));
        return this.blockFeatureStyle;
      }
    })

    this.fieldLayer = new ol.layer.Vector({
      title: 'Fields',
      source: MapComponent._getOgaVectorSource('https://opendata.arcgis.com/datasets/ab4f6b9519794522aa6ffa6c31617bf8_0.geojson'),
      style: feature => {
        this.fieldFeatureStyle.getText().setText(feature.get('FIELDNAME'));
        return this.fieldFeatureStyle;
      }
    })

    this.pipelineLayer = new ol.layer.Vector({
      title: 'Pipelines',
      source: MapComponent._getOgaVectorSource('https://opendata.arcgis.com/datasets/f1934e68cb184c37a21695c0a3c907c1_2.geojson'),
      style: this.pipelineFeatureStyle
    })

    // custom layers should be managed by the consumer
    this.pipelineStartPointFeature = new ol.Feature();
    this.pipelineEndPointFeature = new ol.Feature();
    this.pipelinePointsLayer = new ol.layer.Vector({
      source: new ol.source.Vector({
        features: [
          this.pipelineStartPointFeature,
          this.pipelineEndPointFeature
        ]
      }),
      style: new ol.style.Style({
        image: new ol.style.Circle({
          radius: 8,
          fill: new ol.style.Fill({
            color: 'rgba(0, 255, 0, 0.5)'
          }),
          stroke: new ol.style.Stroke({
            color: 'green',
            width: 2
          })
        })
      })
    });

    this.mousePositionControl = new ol.control.MousePosition({
      coordinateFormat: ol.coordinate.createStringXY(4),
      projection: 'EPSG:4326',
      undefinedHTML: '&nbsp;'
    });

    this.$overlayDomElem = $('#map-overlay');
    this.overlay = new ol.Overlay({
      element: this.$overlayDomElem[0],
      offset: [0, -30]
    });

    this.map = new ol.Map({
      controls: ol.control.defaults().extend([this.mousePositionControl]),
      target: this.targetElementId,
      layers: [
        new ol.layer.Tile({
          source: new ol.source.OSM()
        }),
        this.quadrantLayer,
        this.blockLayer,
        this.fieldLayer,
        this.pipelineLayer,
        this.pipelinePointsLayer
      ],
      overlays: [this.overlay],
      view: new ol.View({
        center: ol.proj.fromLonLat([0.445192, 55.235288]),
        zoom: 5.5
      })
    });

    this.lastHoveredFeature = null;
    this.map.on('pointermove', e => {
      if (this.lastHoveredFeature !== null) {
        this.lastHoveredFeature.setStyle(this.pipelineFeatureStyle);
        this.$overlayDomElem.hide();
        this.lastHoveredFeature = null;
      }
      this.map.forEachFeatureAtPixel(e.pixel, feature => {
        // hack to only target pipeline layer, probably a better way
        if(feature.get('PIPE_NAME') !== undefined) {
          feature.setStyle(MapComponent._getStyle('white', 'white', 4));
          this.$overlayDomElem.text(feature.get('PIPE_NAME'));
          this.$overlayDomElem.show();
          this.overlay.setPosition(e.coordinate);
          this.lastHoveredFeature = feature;
          return true;
        }
      });
    });
  }

  registerLayerEventHandlers() {
    $('#map-layer-quadrants').change((e) => {
      this.quadrantLayer.setVisible(e.currentTarget.checked)
    });

    $('#map-layer-blocks').change((e) => {
      this.blockLayer.setVisible(e.currentTarget.checked)
    });

    $('#map-layer-fields').change((e) => {
      this.fieldLayer.setVisible(e.currentTarget.checked)
    });

    $('#map-layer-pipelines').change((e) => {
      this.pipelineLayer.setVisible(e.currentTarget.checked)
    });
  }

  setPipelineStartPoint(startPointLat, startPointLong) {
    this.pipelineStartPointFeature.set('geometry', new ol.geom.Point(ol.proj.fromLonLat([startPointLong, startPointLat])));
  }

  setPipelineEndPoint(endPointLat, endPointLong) {
    this.pipelineEndPointFeature.set('geometry', new ol.geom.Point(ol.proj.fromLonLat([endPointLong, endPointLat])));
  }

  static _getOgaVectorSource(layerUrl) {
    return new ol.source.Vector({
      url: layerUrl,
      format: new ol.format.GeoJSON(),
      attributions: "Contains information provided by the <a href='https://www.ogauthority.co.uk'>OGA</a>"
    });
  }

  static _getStyle(fillColor, strokeColor = '#319FD3', strokeWidth = 1) {
    return new ol.style.Style({
      fill: new ol.style.Fill({
        color: fillColor
      }),
      stroke: new ol.style.Stroke({
        color: strokeColor,
        width: strokeWidth
      }),
      text: new ol.style.Text({
        font: '12px Calibri,sans-serif',
        fill: new ol.style.Fill({
          color: '#000'
        }),
        stroke: new ol.style.Stroke({
          color: '#fff',
          width: 3
        })
      })
    });
  }

}

