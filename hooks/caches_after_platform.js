//create by 梁仲太 2018-6-28
module.exports = function (ctx) {
    //将相应代码写入activity
    replaceGradleFile(ctx);

    function replaceGradleFile(ctx) {
        const Q = ctx.requireCordovaModule('q');
        const path = ctx.requireCordovaModule('path');
        const fs = ctx.requireCordovaModule('fs');
        const pRoot = ctx.opts.projectRoot;

        const packageJsonPath = path.resolve(__dirname, '../package.json');
        const packageJson = require(packageJsonPath);
        const packageName = 'com/chinamobile/gdwy';
        const appGradle = path.join(pRoot, 'platforms/android/app/build.gradle');
        const mainActivity = path.join(pRoot, 'platforms/android/app/src/main/java/'+packageName+'/MainActivity.java');
        const manifestXml = path.join(pRoot, 'platforms/android/app/src/main/AndroidManifest.xml');

        console.log("--------------caches修改源码开始");
        //如果是android平台
        if (fs.existsSync(appGradle)) {
            const data = fs.readFileSync(manifestXml, 'utf8');
            console.log("--------------修改MainActivity");
            //修改mainActivity
            replace_string_in_file(fs,mainActivity,
            'import android.os.Bundle;',
            'import android.os.Bundle;import com.chinamobile.cache.Caches;');

            replace_string_in_file(fs,mainActivity,
                    'loadUrl(launchUrl);',
                    'Caches.closeAndroidPDialog();loadUrl(launchUrl);');
        }

    }

    //替换文件中的指定内容
    function replace_string_in_file(fs, filename, to_replace, replace_with) {
        const data = fs.readFileSync(filename, 'utf8');
        const result = data.replace(to_replace, replace_with);
        fs.writeFileSync(filename, result, 'utf8');
    }
    //写入文件
    function write_file(fs, source, target) {
        var readable = fs.createReadStream(source);
        var writable = fs.createWriteStream(target);
        readable.pipe(writable);
    }
}