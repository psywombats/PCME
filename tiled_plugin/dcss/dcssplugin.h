/*
 * Tiled DCSS Plugin (psycrawl map editor)
 * Copyright 2017 Aaron King (psy_wombats@wombatrpgs.net)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

#pragma once

#include "dcss_global.h"

#include "mapformat.h"
#include "plugin.h"
#include "tilesetformat.h"

#include <QObject>

namespace Tiled {
class Map;
}

namespace Dcss {

class DCSSSHARED_EXPORT DcssPlugin : public Tiled::Plugin
{
    Q_OBJECT
    Q_PLUGIN_METADATA(IID "org.mapeditor.MapFormat" FILE "plugin.json")

public:
    DcssPlugin();

    bool write(const Tiled::Map *map, const QString &fileName) override;
    QString errorString() const override;
    QString shortName() const override;
    QStringList outputFiles(const Tiled::Map *, const QString &fileName) const override;

protected:
    QString nameFilter() const override;

private:
    QString mError;
};

} // namespace Dcss
