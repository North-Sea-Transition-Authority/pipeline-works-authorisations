const sass = require('gulp-sass')(require('sass'));
const postcss = require('gulp-postcss');
const gulp = require('gulp');
const sourcemaps = require('gulp-sourcemaps');
const autoprefixer = require('autoprefixer');
const rename = require("gulp-rename");
const babel = require("gulp-babel");

gulp.task('copyFdsResources', () => {
  return gulp.src(['fivium-design-system-core/fds/**/*'])
    .pipe(gulp.dest('src/main/resources/templates/fds'));
});

gulp.task('copyGovukResources', () => {
  return gulp.src(['fivium-design-system-core/node_modules/govuk-frontend/**/*'])
    .pipe(gulp.dest('src/main/resources/public/assets/govuk-frontend'));
});

gulp.task('copyFdsJs', () => {
  return gulp.src(['src/main/resources/templates/fds/static/js/**/*'])
    .pipe(gulp.dest('src/main/resources/public/assets/static/fds/js'));
});

gulp.task('copyFdsVendorJs', () => {
  return gulp.src(['src/main/resources/templates/fds/vendor/**/*'])
    .pipe(gulp.dest('src/main/resources/public/assets/static/js/vendor'))
});

// copy FDS images into public/assets
gulp.task('copyFdsImages', () => {
  return gulp.src(['fivium-design-system-core/fds/static/images/**/*'])
    .pipe(gulp.dest('src/main/resources/public/assets/static/fds/images'));
});

gulp.task('initFds', gulp.series(['copyFdsResources', 'copyGovukResources', 'copyFdsImages', 'copyFdsJs', 'copyFdsVendorJs']));

gulp.task('sass', gulp.series(['initFds'], () => {
  const dest = "src/main/resources/public/assets/static/css";
  const sassGlobPattern = "src/main/resources/scss/*.scss";
  const sassOptions = {
    outputStyle: "compressed",
    includePath: "src/main/resources/scss"
  };

  return gulp.src(sassGlobPattern, {base: "."})
    .pipe(sourcemaps.init())
    .pipe(sass(sassOptions, true))
    .pipe(postcss([autoprefixer({ grid: true })]))
    .pipe(sourcemaps.write())
    .pipe(rename(path => path.dirname = ""))
    .pipe(gulp.dest(dest));
}));

gulp.task('babel', () => {
  const jsBabelOptions = {
    presets: ["@babel/preset-env"]
  };

  return gulp.src('src/main/resources/public/assets/javascript/pwa/*.js', {base: "."})
    .pipe(babel(jsBabelOptions))
    .pipe(rename(path => {
      path.dirname = path.dirname.replace(/javascript([\/\\])pwa/, '$1static$1js$1pwa');
    }))
    .pipe(gulp.dest('./'))
    .pipe(rename(path => {
      path.dirname = path.dirname.replace(/([\/\\])?src([\/\\])main[\/\\]resources[\/\\]public[\/\\]assets[\/\\]javascript[\/\\]pwa/, '$2out$2production$2resources$2public$2assets$2static$2js$2pwa$2');
    }))
    .pipe(gulp.dest('./'));
});

gulp.task('buildAll', gulp.series(['sass', 'babel']));
