const sass = require('gulp-sass');
const postcss = require('gulp-postcss');
const gulp = require('gulp');
const sourcemaps = require('gulp-sourcemaps');
const autoprefixer = require('autoprefixer');
const rename = require("gulp-rename");
const babel = require("gulp-babel");

const sassOptions = {
  outputStyle: 'expanded',
  includePath: 'src/main/resources/scss'
};

const jsBabelOptions = {
  presets: ["@babel/preset-env"]
};

const jsBabelGlobPattern = 'src/main/resources/public/assets/javascript/pipelines/*.js';

function compileBabel() {
  let babelTask = babel(jsBabelOptions);

  return gulp.src(jsBabelGlobPattern, {base: "."})
    .pipe(babelTask)
    .pipe(rename(path => {
      path.dirname = path.dirname.replace(/javascript([\/\\])pipelines/, '$1static$1js$1pipelines');
    }))
    .pipe(gulp.dest('./'))
        .pipe(rename(path => {
          path.dirname = path.dirname.replace(/([\/\\])?src([\/\\])main[\/\\]resources[\/\\]public[\/\\]assets[\/\\]javascript[\/\\]pipelines/, '$2out$2production$2resources$2public$2assets$2static$2js$2pipelines$2');
    }))
    .pipe(gulp.dest('./'));
}

function compileSass(exitOnError) {
  const plugins = [
    autoprefixer({
      browsers: ['last 3 versions', '> 0.1%', 'Firefox ESR'],
      grid: true
    })
  ];

  let sassTask = sass(sassOptions);
  if (!exitOnError) {
    //Without an error handler specified, the task will exit on error, which we want for the "buildAll" task
    sassTask = sassTask.on('error', sass.logError);
  }

  return gulp.src('src/main/resources/scss/*.scss', {base: "."})
    .pipe(sourcemaps.init())
    .pipe(sassTask)
    .pipe(postcss(plugins))
    .pipe(sourcemaps.write())
    .pipe(rename(path => {
      //E.g. src\main\resources\sass\core -> src\main\resources\public\assets\static\css
      path.dirname = path.dirname.replace(/([\/\\])scss[\/\\]?/, '$1public$1assets$1static$1css');
}))
.pipe(gulp.dest('./'))
  //For IntelliJ run
    .pipe(rename(path => {
      //E.g. src\main\resources\sass\core -> out\production\resources\static\core
      path.dirname = path.dirname.replace(/([\/\\])?src([\/\\])main[\/\\]/, '$2out$2production$2');
}))
.pipe(gulp.dest('./'));
}


gulp.task('copyFdsResources', () => {
  return gulp.src(['fivium-design-system-core/fds/**/*'])
    .pipe(gulp.dest('src/main/resources/templates/fds'));
});

gulp.task('copyGovukResources', () => {
  return gulp.src(['fivium-design-system-core/node_modules/govuk-frontend/**/*'])
    .pipe(gulp.dest('src/main/resources/public/assets/govuk-frontend'));
});

gulp.task('copyHtml5Shiv', () => {
  return gulp.src(['fivium-design-system-core/node_modules/html5shiv/dist/html5shiv.min.js'])
    .pipe(gulp.dest('src/main/resources/public/assets/html5shiv'))
});

gulp.task('initFds', gulp.series(['copyFdsResources', 'copyGovukResources', 'copyHtml5Shiv']));

gulp.task('sass', gulp.series(['initFds'], () => {
  return compileSass(false);
}));

gulp.task('sassCi', gulp.series(['initFds'], () => {
  return compileSass(true);
}));

gulp.task('babel', () => {
  return compileBabel();
});

gulp.task('buildAll', gulp.series(['sassCi', 'babel']));